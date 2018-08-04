package com.slimgears.rxrpc.server;

import org.reactivestreams.Publisher;

public interface EndpointDispatcher {
    Publisher<?> dispatch(String path, InvocationArguments args);
}
