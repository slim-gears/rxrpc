/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.core.util;

import io.reactivex.Emitter;
import io.reactivex.Observer;

public class Emitters {
    public static <T> Emitter<T> fromObserver(Observer<T> observer) {
        return new Emitter<T>() {
            @Override
            public void onNext(T value) {
                observer.onNext(value);
            }

            @Override
            public void onError(Throwable error) {
                observer.onError(error);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        };
    }
}
