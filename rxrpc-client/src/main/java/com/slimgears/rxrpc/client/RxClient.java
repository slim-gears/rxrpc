package com.slimgears.rxrpc.client;

import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.core.api.JsonEngine;
import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.core.data.Result;
import com.slimgears.rxrpc.core.util.MappedFuture;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Maybe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.reactivestreams.Publisher;

import javax.json.JsonObject;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RxClient {
    private final Config config;

    @AutoValue
    public static abstract class Config {
        public abstract MessageChannel.Client client();
        public abstract JsonEngine jsonEngine();
        public abstract EndpointFactory endpointFactory();
        public static Builder builder() {
            return new AutoValue_RxClient_Config.Builder();
        }

        @AutoValue.Builder
        public interface Builder {
            Builder client(MessageChannel.Client client);
            Builder jsonEngine(JsonEngine engine);
            Builder endpointFactory(EndpointFactory factory);
            Config build();
        }
    }

    public interface EndpointResolver {
        <T> T resolve(Class<T> endpointClientClass);
    }

    public interface EndpointFactory {
        <T> T create(Class<T> clientClass, Future<Session> session);
    }

    public interface Session {
        interface Listener {
            void onClosed();
            void onDisconnected();
        }

        interface Subscription extends MessageChannel.Subscription {

        }

        Publisher<Result> invoke(String method, JsonObject args);
        Subscription subscribe(Listener listener);
        Config serverConfig();
    }

    public static RxClient forConfig(Config config) {
        return new RxClient(config);
    }

    private RxClient(Config config) {
        this.config = config;
    }

    public EndpointResolver connect(URI uri) {
        Future<Session> sessionFuture = MappedFuture.of(this.config.client().connect(uri), InternalSession::new);
        return new InternalEndpointResolver(sessionFuture);
    }

    private class InternalEndpointResolver implements EndpointResolver {
        private final Future<Session> session;

        private InternalEndpointResolver(Future<Session> session) {
            this.session = session;
        }

        @Override
        public <T> T resolve(Class<T> endpointClientClass) {
            return config.endpointFactory().create(endpointClientClass, session);
        }
    }

    private class InternalSession implements Session, MessageChannel.Listener {
        private final AtomicReference<MessageChannel.Session> channelSession = new AtomicReference<>();
        private final AtomicLong invocationId = new AtomicLong();
        private final Collection<Listener> listeners = new ArrayList<>();
        private final Map<Long, Subject<Result>> resultSubjects = new HashMap<>();

        private InternalSession(MessageChannel messageChannel) {
            messageChannel.subscribe(this);
        }

        @Override
        public Publisher<Result> invoke(String method, JsonObject args) {
            long id = invocationId.incrementAndGet();
            Subject<Result> subject = BehaviorSubject.create();
            resultSubjects.put(id, subject);
            Invocation invocation = Invocation.builder()
                    .invocationId(id)
                    .method(method)
                    .arguments(args)
                    .build();

            this.channelSession.get().send(config.jsonEngine().encodeString(invocation));

            return subject
                    .doFinally(() -> resultSubjects.remove(id))
                    .flatMapMaybe(this::toMaybe)
                    .toFlowable(BackpressureStrategy.BUFFER);
        }

        private Maybe<Result> toMaybe(Result result) {
            if (result.type() == Result.Type.Complete) {
                return Maybe.empty();
            } else if (result.type() == Result.Type.Error) {
                return Maybe.error(new RuntimeException(result.error().message()));
            } else {
                return Maybe.just(result);
            }
        }

        @Override
        public Subscription subscribe(Listener listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Override
        public Config serverConfig() {
            return config;
        }

        @Override
        public void onConnected(MessageChannel.Session session) {

        }

        @Override
        public void onMessage(String message) {
            Response response = config.jsonEngine().decodeString(message, Response.class);
            Optional
                    .ofNullable(resultSubjects.get(response.invocationId()))
                    .ifPresent(subj -> subj.onNext(response.result()));
        }

        @Override
        public void onClosed() {
            resultSubjects.values().forEach(Observer::onComplete);
            listeners.forEach(Listener::onClosed);
        }

        @Override
        public void onError(Throwable error) {
            new ArrayList<>(resultSubjects.values()).forEach(subj -> subj.onError(error));
            listeners.forEach(Listener::onDisconnected);
        }
    }
}
