package com.slimgears.rxrpc.client;

import com.slimgears.rxrpc.core.api.JsonEngine;
import com.slimgears.rxrpc.core.api.Lazy;
import com.slimgears.rxrpc.core.data.Result;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class AbstractClient {
    private final Single<RxClient.Session> session;
    private final Lazy<JsonEngine> jsonEngine;

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

    protected AbstractClient(Future<RxClient.Session> session) {
        this.session = Single.fromFuture(session);
        this.jsonEngine = Lazy.fromCallable(() -> session.get().serverConfig().jsonEngine());
    }

    protected <T> Observable<T> invokeObservable(Class<T> responseType, String method, InvocationArguments args) {
        return session.toObservable()
                .flatMap(s -> Observable.fromPublisher(
                        s.invoke(method, jsonEngine.get().encode(args.toMap()).asJsonObject())))
                .flatMapMaybe(result -> {
                    if (result.type() == Result.Type.Error) {
                        return Maybe.error(new RuntimeException(result.error().message()));
                    } else if (result.type() == Result.Type.Complete) {
                        return Maybe.empty();
                    } else {
                        return Maybe.just(jsonEngine.get().decode(result.data(), responseType));
                    }
                });
    }

    protected <T> Single<T> invokeSingle(Class<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).singleOrError();
    }

    protected <T> Maybe<T> invokeMaybe(Class<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).singleElement();
    }

    protected Completable invokeCompletable(String method, InvocationArguments args) {
        return invokeObservable(Void.class, method, args).ignoreElements();
    }

    protected <T> Future<T> invokeFuture(Class<T> responseType, String method, InvocationArguments args) {
        return invokeObservable(responseType, method, args).toFuture();
    }
}
