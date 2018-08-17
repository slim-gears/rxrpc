package com.slimgears.rxrpc.core;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.net.URI;

public interface Transport extends AutoCloseable {
    Emitter<String> outgoing();
    Observable<String> incoming();
    default void close() {}

    interface Client {
        Single<Transport> connect(URI uri);
    }

    interface Server {
        Observable<Transport> connections();
    }
}
