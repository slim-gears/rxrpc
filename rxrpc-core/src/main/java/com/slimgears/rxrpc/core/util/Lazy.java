package com.slimgears.rxrpc.core.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T>, AutoCloseable {
    private final AtomicReference<T> instance = new AtomicReference<>();
    private final Supplier<T> supplier;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> fromCallable(Callable<T> supplier) {
        return of(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public T get() {
        return Optional
                .ofNullable(instance.get())
                .orElseGet(() -> {
                    synchronized (instance) {
                        return Optional
                                .ofNullable(instance.get())
                                .orElseGet(() -> {
                                    T instance = supplier.get();
                                    this.instance.set(instance);
                                    return instance;
                                });
                    }
                });
    }

    public <R> Lazy<R> map(Function<? super T, ? extends R> mapper) {
        return of(() -> mapper.apply(supplier.get()));
    }

    public void ifPresent(Consumer<? super T> consumer) {
        Optional.ofNullable(instance.get()).ifPresent(consumer);
    }

    @Override
    public void close() {
        Optional.ofNullable(instance.get())
                .filter(AutoCloseable.class::isInstance)
                .map(AutoCloseable.class::cast)
                .ifPresent(c -> {
                    try {
                        c.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
