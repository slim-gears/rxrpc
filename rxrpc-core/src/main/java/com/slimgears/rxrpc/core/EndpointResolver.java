package com.slimgears.rxrpc.core;

public interface EndpointResolver extends AutoCloseable {
    <T> T resolve(Class<T> cls);
    default void close() {}
}
