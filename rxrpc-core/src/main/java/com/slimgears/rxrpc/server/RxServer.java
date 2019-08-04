package com.slimgears.rxrpc.server;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.core.util.HasObjectMapper;
import com.slimgears.rxrpc.core.util.MoreDisposables;
import com.slimgears.rxrpc.core.util.ObjectMappers;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.ScopedResolver;
import com.slimgears.util.generic.ServiceResolver;
import com.slimgears.util.generic.ServiceResolvers;
import com.google.common.reflect.TypeToken;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.internal.functions.Functions;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.slimgears.rxrpc.core.util.ObjectMappers.toReference;

public class RxServer implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(RxServer.class);
    private final Set<Session> sessions = new HashSet<>();
    private final Config config;
    private final AtomicReference<Disposable> disposable = new AtomicReference<>(Disposables.empty());

    @AutoValue
    public static abstract class Config implements HasObjectMapper {
        public abstract RxTransport.Server server();
        public abstract ServiceResolver resolver();
        public abstract EndpointRouter router();

        public static Builder builder() {
            return new AutoValue_RxServer_Config.Builder()
                    .objectMapper(ObjectMappers::create)
                    .resolver(ServiceResolvers.defaultConstructorResolver());
        }

        @AutoValue.Builder
        public interface Builder extends HasObjectMapper.Builder<Builder> {
            Builder server(RxTransport.Server server);
            Builder resolver(ServiceResolver resolver);
            Builder router(EndpointRouter router);
            Config build();

            default RxServer createServer() {
                return RxServer.forConfig(build());
            }

            default Builder discoverModules() {
                return modules(EndpointRouters.discover());
            }

            default Builder modules(EndpointRouter.Module... modules) {
                return router(EndpointRouters.fromModules(modules));
            }
        }
    }

    public static RxServer forConfig(Config config) {
        return new RxServer(config);
    }
    public static RxServer.Config.Builder configBuilder() {
        return RxServer.Config.builder();
    }

    private RxServer(Config config) {
        this.config = config;
    }

    public void start() {
        this.disposable.set(config.server().connections().subscribe(this::onAcceptTransport));
    }

    public void stop() {
        this.disposable.getAndSet(Disposables.empty()).dispose();
        ImmutableList.copyOf(sessions).forEach(Session::close);
    }

    @Override
    public void close() {
        stop();
    }

    private InvocationArguments toArguments(Invocation invocation) {
        Map<String, JsonNode> args = invocation.arguments();
        return new InvocationArguments() {
            @Override
            public <T> T get(String key, TypeToken<T> type) {
                return Optional
                        .ofNullable(args)
                        .map(args -> args.get(key))
                        .<T>map(json -> {
                            try {
                                return config.objectMapper().readValue(json.traverse(), toReference(type));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            }
        };
    }

    private void onAcceptTransport(RxTransport transport) {
        sessions.add(new Session(transport));
    }

    class Session implements AutoCloseable {
        private final ConcurrentMap<Long, Disposable> activeInvocations = new ConcurrentHashMap<>();
        private final ServiceResolver resolver = ScopedResolver.of(config.resolver());
        private final Disposable disposable;
        private final RxTransport transport;

        Session(RxTransport transport) {
            this.transport = transport;

            Observable<Invocation> invocations = this.transport
                    .incoming()
                    .map(this::toInvocation)
                    .doOnError(e -> transport.outgoing().onError(e))
                    .share();

            this.disposable = MoreDisposables.ofAll(
                    invocations.subscribe(Functions.emptyConsumer(), this::onError, this::close),
                    invocations.filter(Invocation::isSubscription).subscribe(this::handleSubscription),
                    invocations.filter(Invocation::isUnsubscription).subscribe(this::handleUnsubscription),
                    invocations.filter(Invocation::isKeepAlive).subscribe(this::handleKeepAlive));
        }

        private <T> void onDataResponse(Invocation invocation, T response) {
            sendResponse(Response.ofData(invocation.invocationId(), config.objectMapper().valueToTree(response)));
        }

        private void onErrorResponse(Invocation invocation, Throwable error) {
            sendResponse(Response.ofError(invocation.invocationId(), error));
        }

        private void onCompleteResponse(Invocation invocation) {
            sendResponse(Response.ofComplete(invocation.invocationId()));
        }

        private Invocation toInvocation(String msg) throws IOException {
            try {
                return config.objectMapper().readValue(msg, Invocation.class);
            } catch (JsonParseException e) {
                throw new IllegalArgumentException("Unrecognized message received: " + msg, e);
            }
        }

        private void sendResponse(Response response) {
            try {
                String msg = config.objectMapper().writeValueAsString(response);
                transport.outgoing().onNext(msg);
            } catch (JsonProcessingException e) {
                log.error("Error occurred: ", e);
                onError(e);
            }
        }

        private void onClosed() {
            clean();
        }

        private void onError(Throwable error) {
            log.error("Error occurred: ", error);
            clean();
        }

        private synchronized void clean() {
            this.disposable.dispose();
            sessions.remove(this);
            activeInvocations.values().forEach(Disposable::dispose);
            activeInvocations.clear();
        }

        private void handleSubscription(Invocation message) {
            try {
                EndpointRouter router = config.router();
                Publisher<?> response = router
                        .dispatch(resolver, message.method(), toArguments(message));

                //noinspection ResultOfMethodCallIgnored
                Observable
                        .fromPublisher(response)
                        .doOnError(throwable -> log.error("RxServer.handleSubscription: Log uncaught error before sending to client",throwable))
                        .doOnSubscribe(disposable -> activeInvocations.put(message.invocationId(), disposable))
                        .doFinally(() -> activeInvocations.remove(message.invocationId()))
                        .subscribe(
                                val -> onDataResponse(message, val),
                                error -> onErrorResponse(message, error),
                                () -> onCompleteResponse(message));
            } catch (Throwable e) {
                onErrorResponse(message, e);
            }
        }

        private void handleUnsubscription(Invocation message) {
            Optional.ofNullable(activeInvocations.remove(message.invocationId()))
                    .ifPresent(Disposable::dispose);
        }

        private void handleKeepAlive(Invocation message) {
        }

        @Override
        public void close() {
            try {
                transport.outgoing().onComplete();
            } catch (Exception e) {
                log.error("Error occurred when closing server: ", e);
            }
            onClosed();
        }
    }
}
