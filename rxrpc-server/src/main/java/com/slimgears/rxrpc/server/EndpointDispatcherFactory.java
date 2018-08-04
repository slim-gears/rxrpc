package com.slimgears.rxrpc.server;

public interface EndpointDispatcherFactory {
    EndpointDispatcher create(EndpointResolver resolver);
}
