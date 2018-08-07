package com.slimgears.rxrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.core.data.Result;
import com.slimgears.rxrpc.core.util.MappedFuture;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Maybe;
import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;
import io.reactivex.subjects.Subject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RxClient {
    private final static Logger log = LoggerFactory.getLogger(RxClient.class);
    private final Config config;

    @AutoValue
    public static abstract class Config {
        public abstract MessageChannel.Client client();
        public abstract ObjectMapper objectMapper();
        public abstract EndpointFactory endpointFactory();
        public static Builder builder() {
            return new AutoValue_RxClient_Config.Builder();
        }

        @AutoValue.Builder
        public interface Builder {
            Builder client(MessageChannel.Client client);
            Builder objectMapper(ObjectMapper mapper);
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

        Publisher<Result> invoke(String method, Map<String, Object> args);
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
        private final SingleSubject<MessageChannel.Session> channelSession = SingleSubject.create();
        private final AtomicLong invocationId = new AtomicLong();
        private final Collection<Listener> listeners = new ArrayList<>();
        private final Map<Long, Subject<Result>> resultSubjects = new HashMap<>();

        private InternalSession(MessageChannel messageChannel) {
            messageChannel.subscribe(this);
        }

        @Override
        public Publisher<Result> invoke(String method, Map<String, Object> args) {
            long id = invocationId.incrementAndGet();
            Subject<Result> subject = BehaviorSubject.create();
            resultSubjects.put(id, subject);
            Map<String, JsonNode> jsonArgs = args.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> config.objectMapper().valueToTree(e.getValue())));

            Invocation invocation = Invocation.builder()
                    .invocationId(id)
                    .method(method)
                    .arguments(jsonArgs)
                    .build();

            Publisher<Result> resultPublisher = subject
                    .doFinally(() -> resultSubjects.remove(id))
                    .toFlowable(BackpressureStrategy.BUFFER);

            //noinspection ResultOfMethodCallIgnored
            this.channelSession.subscribe(s ->
                    s.send(config.objectMapper().writeValueAsString(invocation)));

            return resultPublisher;
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
            channelSession.onSuccess(session);
        }

        @Override
        public void onMessage(String message) {
            try {
                Response response = config.objectMapper().readValue(message, Response.class);
                log.info("Response received: {}", response);
                Subject<Result> resultSubject = resultSubjects.get(response.invocationId());
                if (resultSubject != null) {
                    resultSubject.onNext(response.result());
                } else {
                    onError(new RuntimeException("Invocation with id " + invocationId + " not found"));
                }
            } catch (IOException e) {
                onError(e);
            }
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
