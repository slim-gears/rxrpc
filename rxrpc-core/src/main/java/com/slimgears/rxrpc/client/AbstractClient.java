package com.slimgears.rxrpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slimgears.rxrpc.core.data.Result;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

public abstract class AbstractClient {
    private final Single<RxClient.Session> session;
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected interface InvocationArguments {
        InvocationArguments put(String name, Object arg);
        Map<String, Object> toMap();
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

    protected AbstractClient(Single<RxClient.Session> session) {
        this.session = session;
    }

    protected <T> Observable<T> invokeObservable(Class<T> responseType, String method, InvocationArguments args) {
        return session
                .toObservable()
                .map(s -> s.invoke(method, args.toMap()))
                .flatMap(Observable::fromPublisher)
                .takeWhile(result -> result.type() != Result.Type.Complete)
                .compose(toValue(responseType));
    }

    private <T> ObservableTransformer<Result, T> toValue(Class<T> valueType) {
        return source -> Observable.create(emitter -> {
            Disposable disposable = source.subscribe(res -> {
                if (res.type() == Result.Type.Data) {
                    handleDataResult(res.data(), emitter, valueType);
                } else {
                    emitter.onError(new RuntimeException(requireNonNull(res.error()).message()));
                }
            }, emitter::onError, emitter::onComplete);
            emitter.setDisposable(disposable);
        });
    }

    private <T> void handleDataResult(JsonNode json, ObservableEmitter<T> emitter, Class<T> valueType) throws JsonProcessingException {
        if (json == null) {
            emitter.onComplete();
            return;
        }
        T data = objectMapper.treeToValue(json, valueType);
        emitter.onNext(data);
    }

    protected <T> Single<T> invokeSingle(Class<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).singleOrError();
    }

    protected <T> Maybe<T> invokeMaybe(Class<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).singleElement();
    }

    protected Completable invokeCompletable(Class responseType, String method, InvocationArguments args) {
        return invokeObservable(Void.class, method, args).ignoreElements();
    }

    protected <T> Future<T> invokeFuture(Class<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).toFuture();
    }

    protected <T> T invokeBlocking(Class<T> responseType, String method, InvocationArguments args) {
        try {
            return invokeFuture(responseType, method, args).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
