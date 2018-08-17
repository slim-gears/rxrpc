package com.slimgears.rxrpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.core.EndpointResolver;
import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.core.data.Result;
import com.slimgears.rxrpc.core.util.HasObjectMapper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RxClient {
    private final static Logger log = LoggerFactory.getLogger(RxClient.class);
    private final Config config;

    @AutoValue
    public static abstract class Config implements HasObjectMapper {
        public abstract Transport.Client client();
        public abstract EndpointFactory factory();
        public static Builder builder() {
            return new AutoValue_RxClient_Config.Builder()
                    .objectMapperProvider(ObjectMapper::new)
                    .factory(EndpointFactories.constructorFactory());
        }

        @AutoValue.Builder
        public interface Builder extends HasObjectMapper.Builder<Builder> {
            Builder client(Transport.Client client);
            Builder factory(EndpointFactory factory);
            Config build();

            default RxClient createClient() {
                return RxClient.forConfig(build());
            }
        }
    }

    public interface EndpointFactory {
        <T> T create(Class<T> clientClass, Single<Session> session);
    }

    public interface Session extends AutoCloseable {
        Publisher<Result> invoke(String method, Map<String, Object> args);
    }

    public static RxClient forConfig(Config config) {
        return new RxClient(config);
    }
    public static RxClient.Config.Builder configBuilder() {
        return RxClient.Config.builder();
    }

    private RxClient(Config config) {
        this.config = config;
    }

    public EndpointResolver connect(URI uri) {
        Single<Session> session = this.config.client().connect(uri).map(InternalSession::new);
        return new InternalEndpointResolver(session);
    }

    private class InternalEndpointResolver implements EndpointResolver {
        private final Single<Session> session;

        private InternalEndpointResolver(Single<Session> session) {
            this.session = session;
        }

        @Override
        public <T> T resolve(Class<T> endpointClientClass) {
            return config.factory().create(endpointClientClass, session);
        }
    }

    private class InternalSession implements Session {
        private final Transport transport;
        private final Disposable disposable;
        private final AtomicLong invocationId = new AtomicLong();
        private final Map<Long, Subject<Result>> resultSubjects = new HashMap<>();

        private InternalSession(Transport transport) {
            this.transport = transport;
            this.disposable = this.transport.incoming().subscribe(this::onMessage, this::onError, this::onClosed);
        }

        @Override
        public Publisher<Result> invoke(String method, Map<String, Object> args) {
            return Observable
                    .defer(() -> beginInvocation(method, args))
                    .toFlowable(BackpressureStrategy.BUFFER);
        }

        private void onMessage(String message) {
            try {
                Response response = config.objectMapper().readValue(message, Response.class);
                log.debug("Response received: {}", response);
                Optional.ofNullable(resultSubjects.get(response.invocationId()))
                        .ifPresent(subj -> subj.onNext(response.result()));
            } catch (IOException e) {
                onError(e);
            }
        }

        private void onClosed() {
            resultSubjects.values().forEach(Observer::onComplete);
        }

        private void onError(Throwable error) {
            new ArrayList<>(resultSubjects.values()).forEach(subj -> subj.onError(error));
        }

        private Observable<Result> beginInvocation(String method, Map<String, Object> args) {
            Subject<Result> subject = BehaviorSubject.create();
            Map<String, JsonNode> jsonArgs = args.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> config.objectMapper().valueToTree(e.getValue())));
            long id = invocationId.incrementAndGet();
            Invocation invocation = Invocation.builder()
                    .invocationId(id)
                    .method(method)
                    .arguments(jsonArgs)
                    .build();
            return subject
                    .doOnLifecycle(
                            d -> {
                                resultSubjects.put(id, subject);
                                sendInvocation(invocation);
                            },
                            () -> sendInvocation(Invocation.ofCancellation(id)))
                    .doFinally(() -> resultSubjects.remove(id))
                    .share();
        }

        private void sendInvocation(Invocation invocation) {
            try {
                this.transport.outgoing().onNext(config.objectMapper().writeValueAsString(invocation));
            } catch (JsonProcessingException e) {
                this.transport.outgoing().onError(e);
            }
        }

        @Override
        public void close() {
            transport.outgoing().onComplete();
            disposable.dispose();
        }
    }
}
