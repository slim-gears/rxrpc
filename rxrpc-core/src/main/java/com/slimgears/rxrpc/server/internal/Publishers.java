package com.slimgears.rxrpc.server.internal;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.reactivestreams.Publisher;

import java.util.concurrent.Future;

public class Publishers {
    public static <T> Publisher<T> toPublisher(Observable<T> observable) {
        return observable.toFlowable(BackpressureStrategy.BUFFER);
    }

    public static <T> Publisher<T> toPublisher(Publisher<T> publisher) {
        return publisher;
    }

    public static <T> Publisher<T> toPublisher(Flowable<T> publisher) {
        return publisher;
    }

    public static <T> Publisher<T> toPublisher(Single<T> single) {
        return single.toFlowable();
    }

    public static <T> Publisher<T> toPublisher(Future<T> future) {
        return toPublisher(Maybe.fromFuture(future));
    }

    public static <T> Publisher<T> toPublisher(Maybe<T> maybe) {
        return maybe.toFlowable();
    }

    public static Publisher<Void> toPublisher(Completable completable) {
        return completable.toFlowable();
    }

    public static <T> Publisher<T> toPublisher(T value) {
        return toPublisher(Single.just(value));
    }
}
