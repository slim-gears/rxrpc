package com.slimgears.rxrpc.core.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Notifier<L> {
    private final List<L> listeners = new CopyOnWriteArrayList<>();

    public interface Subscription {
        void unsubscribe();
    }

    public Subscription subscribe(L listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void publish(Consumer<L> notification) {
        listeners.forEach(notification);
    }
}
