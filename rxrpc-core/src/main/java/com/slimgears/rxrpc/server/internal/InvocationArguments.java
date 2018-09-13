package com.slimgears.rxrpc.server.internal;

import com.slimgears.util.reflect.TypeToken;

public interface InvocationArguments {
    <T> T get(String key, TypeToken<T> cls);
}
