package com.slimgears.rxrpc.server;

import org.reactivestreams.Publisher;

public interface MethodDispatcher<T, R> {
    Publisher<R> dispatch(T target, InvocationArguments args);
}
