package com.slimgears.rxrpc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.slimgears.rxrpc.core.EndpointResolver;
import com.slimgears.rxrpc.core.EndpointResolvers;
import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.core.util.HasObjectMapper;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.ScopedResolver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class RxServer implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(RxServer.class);
    private final Set<Session> sessions = new HashSet<>();
    private final Config config;
    private final AtomicReference<Disposable> disposable = new AtomicReference<>(Disposables.empty());

    @AutoValue
    public static abstract class Config implements HasObjectMapper {
        public abstract Transport.Server server();
        public abstract EndpointResolver resolver();
        public abstract EndpointDispatcher.Factory dispatcherFactory();

        public static Builder builder() {
            return new AutoValue_RxServer_Config.Builder()
                    .objectMapper(ObjectMapper::new)
                    .resolver(EndpointResolvers.defaultConstructorResolver());
        }

        @AutoValue.Builder
        public interface Builder extends HasObjectMapper.Builder<Builder> {
            Builder server(Transport.Server server);
            Builder resolver(EndpointResolver resolver);
            Builder dispatcherFactory(EndpointDispatcher.Factory factory);
            Config build();

            default RxServer createServer() {
                return RxServer.forConfig(build());
            }

            default Builder discoverModules() {
                return modules(EndpointDispatchers.discover());
            }

            default Builder modules(EndpointDispatcher.Module... modules) {
                return dispatcherFactory(EndpointDispatchers.factoryFromModules(modules));
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

    private InvocationArguments toArguments(Map<String, JsonNode> args) {
        return new InvocationArguments() {
            @Override
            public <T> T get(String key, Class<T> cls) {
                return Optional
                        .ofNullable(args.get(key))
                        .map(json -> {
                            try {
                                return config.objectMapper().treeToValue(json, cls);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElseThrow(() -> new IllegalArgumentException("Argument " + key + " not found"));
            }
        };
    }

    private void onAcceptTransport(Transport transport) {
        sessions.add(new Session(transport));
    }

    class Session implements AutoCloseable {
        private final ConcurrentMap<Long, Disposable> activeInvocations = new ConcurrentHashMap<>();
        private final EndpointResolver resolver = ScopedResolver.of(config.resolver());
        private final Disposable disposable;
        private final Transport transport;

        Session(Transport transport) {
            this.transport = transport;
            this.disposable = this.transport
                    .incoming()
                    .subscribe(this::onMessage, this::onError, this::onClosed);
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

        private void sendResponse(Response response) {
            try {
                String msg = config.objectMapper().writeValueAsString(response);
                transport.outgoing().onNext(msg);
            } catch (JsonProcessingException e) {
                log.error("Error occurred: ", e);
                onError(e);
            }
        }

        private void onMessage(String message) {
            try {
                Invocation invocation = config.objectMapper().readValue(message, Invocation.class);
                try {
                    if (invocation.method() == null) {
                        Optional.ofNullable(activeInvocations.remove(invocation.invocationId()))
                                .ifPresent(Disposable::dispose);
                        return;
                    }

                    EndpointDispatcher dispatcher = config.dispatcherFactory().create(resolver);
                    Publisher<?> response = dispatcher
                            .dispatch(invocation.method(), toArguments(invocation.arguments()));

                    //noinspection ResultOfMethodCallIgnored
                    Observable
                            .fromPublisher(response)
                            .doOnSubscribe(disposable -> activeInvocations.put(invocation.invocationId(), disposable))
                            .doFinally(() -> activeInvocations.remove(invocation.invocationId()))
                            .subscribe(
                                    val -> onDataResponse(invocation, val),
                                    error -> onErrorResponse(invocation, error),
                                    () -> onCompleteResponse(invocation));
                } catch (Throwable e) {
                    onErrorResponse(invocation, e);
                }
            } catch (IOException e) {
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
