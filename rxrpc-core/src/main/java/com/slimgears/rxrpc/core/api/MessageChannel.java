package com.slimgears.rxrpc.core.api;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface MessageChannel {
    interface Session extends AutoCloseable {
        void send(String message);
        void close();
    }

    interface Listener {
        void onConnected(Session session);
        void onMessage(String message);
        void onClosed();
        void onError(Throwable error);
    }

    interface Subscription {
        Subscription EMPTY = () -> {};
        void unsubscribe();
    }

    interface Client {
        Future<MessageChannel> connect(URI uri);
    }

    interface Server {
        Subscription subscribe(Consumer<MessageChannel> listener);
        void start();
        void stop();
    }

    Subscription subscribe(Listener listener);
}
