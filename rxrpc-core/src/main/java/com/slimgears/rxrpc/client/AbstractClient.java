package com.slimgears.rxrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.slimgears.rxrpc.core.data.Result;
import com.slimgears.util.reflect.TypeToken;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.slimgears.rxrpc.core.util.ObjectMappers.toReference;
import static java.util.Objects.requireNonNull;

public abstract class AbstractClient implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(AbstractClient.class);
    private final static TypeToken<Void> voidType = TypeToken.of(Void.class);
    private final RxClient.Session session;

    protected interface InvocationArguments {
        InvocationArguments put(String name, Object arg);
        Map<String, Object> toMap();
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

    protected InvocationArguments arguments() {
        Map<String, Object> args = new HashMap<>();

        return new InvocationArguments() {
            @Override
            public InvocationArguments put(String name, Object arg) {
                args.put(name, arg);
                return this;
            }

            @Override
            public Map<String, Object> toMap() {
                return args;
            }
        };
    }

    protected <T> Observable<T> invokeObservable(TypeToken<T> responseType, String method, InvocationArguments args) {
        return Observable
                .fromPublisher(session.invoke(method, args.toMap()))
                .takeWhile(result -> result.type() != Result.Type.Complete)
                .doOnError(e -> log.warn("Error when executing {}({}): {}", method, args.toString(), e))
                .compose(toValue(responseType));
    }

    private <T> ObservableTransformer<Result, T> toValue(TypeToken<T> valueType) {
        return source -> Observable.create(emitter -> {
            Disposable disposable = source.subscribe(res -> {
                if (res.type() == Result.Type.Data) {
                    handleDataResult(res.data(), emitter, valueType);
                } else {
                    emitter.onError(requireNonNull(res.error()).toException());
                }
            }, emitter::onError, emitter::onComplete);
            emitter.setDisposable(disposable);
        });
    }

    private <T> void handleDataResult(JsonNode json, ObservableEmitter<T> emitter, TypeToken<T> valueType) throws IOException {
        if (json == null) {
            emitter.onComplete();
            return;
        }
        T data = session.clientConfig().objectMapper().readValue(json.traverse(), toReference(valueType));
        emitter.onNext(data);
    }

    protected <T> Single<T> invokeSingle(TypeToken<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).singleOrError();
    }

    protected <T> Maybe<T> invokeMaybe(TypeToken<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).singleElement();
    }

    protected Completable invokeCompletable(TypeToken type, String method, InvocationArguments args) {
        return invokeObservable(voidType, method, args).ignoreElements();
    }

    protected <T> Future<T> invokeFuture(TypeToken<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).toFuture();
    }

    protected <T> T invokeBlocking(TypeToken<T> responseType, String method, InvocationArguments args) {
        try {
            return invokeFuture(responseType, method, args).get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Error occurred: {}", e);
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
