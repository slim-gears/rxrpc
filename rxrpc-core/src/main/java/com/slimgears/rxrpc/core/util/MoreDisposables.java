/**
 *
 */
package com.slimgears.rxrpc.core.util;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

import java.util.stream.Stream;

public class MoreDisposables {
    public static Disposable ofAll(Disposable... disposables) {
        return Disposables.fromAction(() -> Stream.of(disposables).forEach(Disposable::dispose));
    }
}
