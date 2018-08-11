package com.slimgears.rxrpc.core;

import java.net.URI;
import java.util.concurrent.Future;

public interface Transport {
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
        Future<Transport> connect(URI uri);
    }

    interface Server {
        interface Listener {
            void onAcceptTransport(Transport transport);
            void onTerminate();
        }

        Subscription subscribe(Listener listener);
    }

    Subscription subscribe(Listener listener);
}
