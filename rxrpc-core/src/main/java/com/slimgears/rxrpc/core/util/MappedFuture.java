package com.slimgears.rxrpc.core.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class MappedFuture<T, R> implements Future<R> {
    private final Future<T> source;
    private final Function<T, R> mapping;

    public static <T, R> Future<R> of(Future<T> source, Function<T, R> mapping) {
        return new MappedFuture<>(source, mapping);
    }

    private MappedFuture(Future<T> source, Function<T, R> mapping) {
        this.source = source;
        this.mapping = mapping;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return source.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return source.isCancelled();
    }

    @Override
    public boolean isDone() {
        return source.isDone();
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        return mapping.apply(source.get());
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mapping.apply(source.get(timeout, unit));
    }
}
