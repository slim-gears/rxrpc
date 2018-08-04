package com.slimgears.rxrpc.core.api;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface MessageChannel extends AutoCloseable {
    interface Listener {
        void onMessage(String message);
        void onClosed();
        void onError(Throwable error);
    }

    interface Subscription {
        static final Subscription EMPTY = () -> {};
        void unsubscribe();
    }

    interface Client {
        Future<MessageChannel> create(URI uri);
    }

    interface Server {
        Subscription subscribe(Consumer<MessageChannel> listener);
    }

    Subscription subscribe(Listener listener);
    void send(String message);
}
