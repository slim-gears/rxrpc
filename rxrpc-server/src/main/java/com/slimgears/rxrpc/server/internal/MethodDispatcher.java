package com.slimgears.rxrpc.server.internal;

import org.reactivestreams.Publisher;

public interface MethodDispatcher<T, R> {
    Publisher<R> dispatch(T target, InvocationArguments args);
}
