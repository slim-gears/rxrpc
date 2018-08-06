package com.slimgears.rxrpc.server;

import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.core.api.*;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class RxServer implements AutoCloseable {
    private final AtomicReference<MessageChannel.Subscription> subscription = new AtomicReference<>(MessageChannel.Subscription.EMPTY);
    private final Set<Session> sessions = new HashSet<>();
    private final Config config;

    @AutoValue
    public static abstract class Config {
        public abstract MessageChannel.Server server();
        public abstract JsonEngine jsonEngine();
        public abstract EndpointDispatcherFactory dispatcherFactory();
        public abstract EndpointResolver resolver();

        public static Builder builder() {
            return new AutoValue_RxServer_Config.Builder();
        }

        @AutoValue.Builder
        public interface Builder {
            Builder server(MessageChannel.Server server);
            Builder jsonEngine(JsonEngine engine);
            Builder dispatcherFactory(EndpointDispatcherFactory dispatcherFactory);
            Builder resolver(EndpointResolver resolver);
            Config build();
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
        subscription.getAndSet(MessageChannel.Subscription.EMPTY).unsubscribe();
        Collection<Session> currentSessions = new ArrayList<>(sessions);
        currentSessions.forEach(Session::close);
    }

    @Override
    public void close() {
        stop();
    }

    private InvocationArguments toArguments(JsonObject argObject) {
        return new InvocationArguments() {
            @Override
            public <T> T get(String key, Class<T> cls) {
                return config.jsonEngine().decode(argObject.get(key), cls);
            }
        };
    }

    private void onNewChannel(MessageChannel channel) {
        channel.subscribe(new Session(channel));
    }

    class Session implements MessageChannel.Listener, AutoCloseable {
        private final ConcurrentMap<Long, Disposable> activeInvocations = new ConcurrentHashMap<>();
        private final EndpointResolver resolver = ScopedResolver.of(config.resolver());
        private final AtomicReference<MessageChannel.Session> channelSession = new AtomicReference<>();

        Session(MessageChannel channel) {
            channel.subscribe(this);
        }

        private <T> void onDataResponse(Invocation invocation, T response) {
            sendResponse(Response.ofData(invocation.invocationId(), config.jsonEngine().encode(response)));
        }

        private void onErrorResponse(Invocation invocation, Throwable error) {
            sendResponse(Response.ofError(invocation.invocationId(), error));
        }

        private void onCompleteResponse(Invocation invocation) {
            sendResponse(Response.ofComplete(invocation.invocationId()));
        }

        private void sendResponse(Response response) {
            channelSession.get().send(config.jsonEngine().encodeString(response));
        }

        @Override
        public void onConnected(MessageChannel.Session session) {
            channelSession.set(session);
        }

        @Override
        public void onMessage(String message) {
            Invocation invocation = config.jsonEngine().decodeString(message, Invocation.class);
            try {
                Publisher<?> response = config.dispatcherFactory().create(resolver).dispatch(invocation.method(), toArguments(invocation.arguments()));
                Disposable subscription = Observable.fromPublisher(response).subscribe(
                        val -> onDataResponse(invocation, val),
                        error -> onErrorResponse(invocation, error),
                        () -> onCompleteResponse(invocation));
                activeInvocations.put(invocation.invocationId(), subscription);
            } catch (Throwable e) {
                onErrorResponse(invocation, e);
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
