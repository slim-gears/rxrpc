package com.slimgears.rxrpc.core;

public interface ServiceResolver extends AutoCloseable {
    <T> T resolve(Class<T> cls);
    default void close() {}
}
