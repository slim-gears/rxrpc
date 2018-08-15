package com.slimgears.rxrpc.server;

public interface EndpointResolver extends AutoCloseable {
    <T> T resolve(Class<T> cls);
    default void close() {}
}
