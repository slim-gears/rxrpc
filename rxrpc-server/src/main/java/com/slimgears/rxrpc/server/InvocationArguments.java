package com.slimgears.rxrpc.server;

public interface InvocationArguments {
    <T> T get(String key, Class<T> cls);
}
