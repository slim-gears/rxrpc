package com.slimgears.rxrpc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.ScopedResolver;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
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
    private final AtomicReference<Transport.Subscription> subscription = new AtomicReference<>(Transport.Subscription.EMPTY);
    private final Set<Session> sessions = new HashSet<>();
    private final Config config;

    @AutoValue
    public static abstract class Config {
        public abstract Transport.Server server();
        public abstract ObjectMapper objectMapper();
        public abstract EndpointResolver resolver();
        public abstract EndpointDispatcher.Factory dispatcherFactory();

        public static Builder builder() {
            return new AutoValue_RxServer_Config.Builder();
        }

        @AutoValue.Builder
        public interface Builder {
            Builder server(Transport.Server server);
            Builder objectMapper(ObjectMapper mapper);
            Builder resolver(EndpointResolver resolver);
            Builder dispatcherFactory(EndpointDispatcher.Factory factory);
            Config build();

            default Builder modules(EndpointDispatcher.Module... modules) {
                return dispatcherFactory(EndpointDispatchers.factoryFromModules(modules));
            }
        }
    }

    public static RxServer forConfig(Config config) {
        return new RxServer(config);
    }

    private RxServer(Config config) {
        this.config = config;
    }

    public void start() {
        subscription.set(config.server().subscribe(this::onNewChannel));
        config.server().start();
    }

    public void stop() {
        config.server().stop();
        subscription.getAndSet(Transport.Subscription.EMPTY).unsubscribe();
        Collection<Session> currentSessions = new ArrayList<>(sessions);
        currentSessions.forEach(Session::close);
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

    private void onNewChannel(Transport channel) {
        channel.subscribe(new Session());
    }

    class Session implements Transport.Listener, AutoCloseable {
        private final ConcurrentMap<Long, Disposable> activeInvocations = new ConcurrentHashMap<>();
        private final EndpointResolver resolver = ScopedResolver.of(config.resolver());
        private final AtomicReference<Transport.Session> channelSession = new AtomicReference<>();

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
                channelSession.get().send(msg);
            } catch (JsonProcessingException e) {
                log.error("Error occurred: ", e);
                onError(e);
            }
        }

        @Override
        public void onConnected(Transport.Session session) {
            channelSession.set(session);
        }

        @Override
        public void onMessage(String message) {
            try {
                Invocation invocation = config.objectMapper().readValue(message, Invocation.class);
                try {
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

        @Override
        public void onClosed() {
            clean();
        }

        @Override
        public void onError(Throwable error) {
            clean();
        }

        private synchronized void clean() {
            sessions.remove(this);
            activeInvocations.values().forEach(Disposable::dispose);
            activeInvocations.clear();
        }

        @Override
        public void close() {
            try {
                channelSession.get().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            onClosed();
        }
    }
}
