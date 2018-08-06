package com.slimgears.rxrpc.core.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ErrorFuture<T> implements Future<T> {
    private final Throwable error;

    public static <T> ErrorFuture<T> of(Throwable e) {
        return new ErrorFuture<>(e);
    }

    private ErrorFuture(Throwable error) {
        this.error = error;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        throw new ExecutionException(error);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new ExecutionException(error);
    }
}
