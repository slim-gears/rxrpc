package com.slimgears.rxrpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.data.Invocation;
import com.slimgears.rxrpc.core.data.Response;
import com.slimgears.rxrpc.core.data.Result;
import com.slimgears.rxrpc.core.util.HasObjectMapper;
import com.slimgears.rxrpc.core.util.MoreDisposables;
import com.slimgears.util.generic.ServiceResolver;
import com.google.common.reflect.TypeToken;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RxClient {
    private final static Logger log = LoggerFactory.getLogger(RxClient.class);
    private final Config config;

    @AutoValue
    public static abstract class Config implements HasObjectMapper {
        private final static Duration defaultKeepAlive = Duration.ofSeconds(60);

        public abstract RxTransport.Client client();
        public abstract EndpointFactory endpointFactory();
        public abstract SubjectFactory subjectFactory();
        public abstract Duration keepAlivePeriod();
        public static Builder builder() {
            return new AutoValue_RxClient_Config.Builder()
                    .objectMapperProvider(ObjectMapper::new)
                    .endpointFactory(EndpointFactories.constructorFactory())
                    .keepAlivePeriod(defaultKeepAlive)
                    .subjectFactory(ReplaySubject::create);
        }

        @AutoValue.Builder
        public interface Builder extends HasObjectMapper.Builder<Builder> {
            Builder client(RxTransport.Client client);
            Builder endpointFactory(EndpointFactory factory);
            Builder subjectFactory(SubjectFactory factory);
            Builder keepAlivePeriod(Duration period);
            Config build();

            default RxClient createClient() {
                return RxClient.forConfig(build());
            }
        }
    }

    public interface SubjectFactory {
        <T> Subject<T> create();
    }

    public interface EndpointFactory {
        <T> T create(TypeToken<T> clientClass, Session session);
        boolean canCreate(TypeToken<?> clientClass);
    }

    public interface Session extends AutoCloseable {
        Publisher<Result> invoke(String method, Map<String, Object> args);
        Config clientConfig();
        void close();
    }

    public static RxClient forClient(RxTransport.Client client) {
        return configBuilder().client(client).createClient();
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

    public Single<ServiceResolver> connect(URI uri) {
        Single<RxTransport> transport = this.config.client().connect(uri);
        return transport
                .<ServiceResolver>map(tr -> new InternalEndpointResolver(new InternalSession(tr)))
                .cache();
    }

    private class InternalEndpointResolver implements ServiceResolver {
        private final Session session;

        private InternalEndpointResolver(Session session) {
            this.session = session;
        }

        @Override
        public <T> T resolve(TypeToken<T> endpointToken) {
            return config.endpointFactory().create(endpointToken, session);
        }

        @Override
        public boolean canResolve(TypeToken<?> typeToken) {
            return config.endpointFactory().canCreate(typeToken);
        }

        public void close() {
            try {
                session.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class InternalSession implements Session {
        private final RxTransport transport;
        private final Disposable disposable;
        private final AtomicLong invocationId = new AtomicLong();
        private final Map<Long, Subject<Result>> resultSubjects = new HashMap<>();

        private InternalSession(RxTransport transport) {
            this.transport = transport;
            Observable<Response> responses = this.transport
                    .incoming()
                    .map(this::toResponse);

            this.disposable = MoreDisposables.ofAll(
                    responses.subscribe(this::onResponse, this::onError, this::onClosed),
                    Observable
                            .interval(config.keepAlivePeriod().toMillis(), TimeUnit.MILLISECONDS)
                            .forEach(i -> sendKeepAlive()));
        }

        @Override
        public Publisher<Result> invoke(String method, Map<String, Object> args) {
            return Observable
                    .defer(() -> beginInvocation(method, args))
                    .toFlowable(BackpressureStrategy.BUFFER);
        }

        @Override
        public Config clientConfig() {
            return config;
        }

        private void onResponse(Response response) {
            log.debug("Response received: {}", response);
            Optional.ofNullable(resultSubjects.get(response.invocationId()))
                        .ifPresent(subj -> subj.onNext(response.result()));
        }

        private Response toResponse(String msg) throws IOException {
            return config.objectMapper().readValue(msg, Response.class);
        }

        private void onClosed() {
            ImmutableList.copyOf(resultSubjects.values()).forEach(Observer::onComplete);
            close();
        }

        private void onError(Throwable error) {
            new ArrayList<>(resultSubjects.values()).forEach(subj -> subj.onError(error));
            close();
        }

        private Observable<Result> beginInvocation(String method, Map<String, Object> args) {
            Subject<Result> subject = config.subjectFactory().create();
            Map<String, JsonNode> jsonArgs = args.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> config.objectMapper().valueToTree(e.getValue())));
            long id = invocationId.incrementAndGet();
            Invocation invocation = Invocation.ofSubscription(id, method, jsonArgs);
            resultSubjects.put(id, subject);
            return subject
                    .doOnLifecycle(
                            d -> sendInvocation(invocation),
                            () -> sendInvocation(Invocation.ofUnsubscription(id)))
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

        private void sendKeepAlive() throws IOException {
            this.transport.outgoing().onNext(config.objectMapper().writeValueAsString(Invocation.ofKeepAlive()));
        }

        @Override
        public void close() {
            transport.outgoing().onComplete();
            disposable.dispose();
        }
    }
}
