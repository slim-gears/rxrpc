package com.slimgears.rxrpc.server.internal;

public interface InvocationArguments {
    <T> T get(String key, Class<T> cls);
}
