package com.slimgears.rxrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.core.data.Result;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.slimgears.rxrpc.core.util.ObjectMappers.toReference;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractClient implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(AbstractClient.class);
    protected final static TypeToken<Void> voidType = TypeToken.of(Void.class);
    private final RxClient.Session session;

    @AutoValue
    protected static abstract class InvocationInfo<T> {
        public abstract boolean shared();
        public abstract int sharedReplayCount();
        public abstract TypeToken<T> responseType();
        public abstract String method();
        public abstract ImmutableMap<String, Object> args();
        public abstract Builder<T> toBuilder();

        public static <T> Builder<T> builder(TypeToken<T> typeToken) {
            return new AutoValue_AbstractClient_InvocationInfo.Builder<T>()
                    .responseType(typeToken);
        }

        @AutoValue.Builder
        public interface Builder<T> {
            Builder<T> shared(boolean shared);
            Builder<T> sharedReplayCount(int count);
            Builder<T> responseType(TypeToken<T> type);
            Builder<T> method(String method);
            ImmutableMap.Builder<String, Object> argsBuilder();
            InvocationInfo<T> build();

            default Builder<T> arg(String name, Object value) {
                argsBuilder().put(name, value);
                return this;
            }

            default Builder<T> args(Map<String, Object> args) {
                argsBuilder().putAll(args);
                return this;
            }
        }
    }

    protected AbstractClient(RxClient.Session session) {
        this.session = session;
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> Flowable<T> invokeFlowable(InvocationInfo<T> invocationInfo) {
        return Flowable
                .fromPublisher(session.invoke(invocationInfo.method(), invocationInfo.args()))
                .takeWhile(result -> result.type() != Result.Type.Complete)
                .doOnError(e -> log.warn("Error when executing {}: {}", invocationInfo, e))
                .compose(toValue(invocationInfo.responseType()));
    }

    protected <T> Observable<T> invokeObservable(InvocationInfo<T> invocationInfo) {
        return invokeFlowable(invocationInfo).toObservable();
    }

    private <T> FlowableTransformer<Result, T> toValue(TypeToken<T> valueType) {
        return source -> Flowable.create(emitter -> {
            Disposable disposable = source.subscribe(res -> {
                if (res.type() == Result.Type.Data) {
                    handleDataResult(res.data(), emitter, valueType);
                } else {
                    emitter.onError(requireNonNull(res.error()).toException());
                }
            }, emitter::onError, emitter::onComplete);
            emitter.setDisposable(disposable);
        }, BackpressureStrategy.BUFFER);
    }

    private <T> void handleDataResult(JsonNode json, FlowableEmitter<T> emitter, TypeToken<T> valueType) throws IOException {
        if (json == null) {
            emitter.onComplete();
            return;
        }
        T data = session.clientConfig().objectMapper().readValue(json.traverse(), toReference(valueType));
        emitter.onNext(data);
    }

    protected <T> Single<T> invokeSingle(InvocationInfo<T> invocationInfo) {
        return invokeObservable(invocationInfo).singleOrError();
    }

    protected <T> Maybe<T> invokeMaybe(InvocationInfo<T> invocationInfo) {
        return invokeObservable(invocationInfo).singleElement();
    }

    protected Completable invokeCompletable(InvocationInfo<Void> invocationInfo) {
        return invokeObservable(invocationInfo).ignoreElements();
    }

    protected <T> Future<T> invokeFuture(InvocationInfo<T> invocationInfo) {
        return invokeObservable(invocationInfo).toFuture();
    }

    protected <T>Publisher<T> invokePublisher(InvocationInfo<T> invocationInfo) {
        return invokeFlowable(invocationInfo);
    }

    protected <T> T invokeBlocking(InvocationInfo<T> invocationInfo) {
        try {
            return invokeFuture(invocationInfo).get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Error occurred: ", e);
            if (e.getCause() != null) {
                this.<RuntimeException>rethrow(e.getCause());
            } else {
                this.<RuntimeException>rethrow(e);
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> T rethrow(Throwable e) throws T {
        throw (T)e;
    }
}
