package com.slimgears.rxrpc.server.internal;

import com.slimgears.util.generic.ServiceResolver;
import org.reactivestreams.Publisher;

import java.util.function.Supplier;

public interface MethodDispatcher<T, R> {
    Publisher<R> dispatch(ServiceResolver resolver, T target, InvocationArguments args);

    interface Decorator {
        <R> Publisher<R> decorate(Supplier<Publisher<R>> publisher, ServiceResolver resolver);
    }
}
