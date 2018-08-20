package com.slimgears.rxrpc.server.internal;

import com.slimgears.rxrpc.core.EndpointResolver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ScopedResolver implements EndpointResolver {
    private final ConcurrentMap<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private final EndpointResolver sourceResolver;

    private ScopedResolver(EndpointResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(Class<T> cls) {
        return (T)instances.computeIfAbsent(cls, a -> sourceResolver.resolve(cls));
    }

    @Override
    public void close() {
        instances.values().stream()
                .filter(AutoCloseable.class::isInstance)
                .map(AutoCloseable.class::cast)
                .forEach(c -> {
                    try {
                        c.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        sourceResolver.close();
    }

    public static EndpointResolver of(EndpointResolver resolver) {
        return new ScopedResolver(resolver);
    }
}
