package com.slimgears.rxrpc.server.internal;

import com.slimgears.util.generic.ServiceResolver;
import com.google.common.reflect.TypeToken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ScopedResolver implements ServiceResolver {
    private final ConcurrentMap<TypeToken<?>, Object> instances = new ConcurrentHashMap<>();
    private final ServiceResolver sourceResolver;

    private ScopedResolver(ServiceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(TypeToken<T> token) {
        return (T)instances.computeIfAbsent(token, a -> sourceResolver.resolve(token));
    }

    @Override
    public boolean canResolve(TypeToken<?> typeToken) {
        return instances.containsKey(typeToken) || sourceResolver.canResolve(typeToken);
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

    public static ServiceResolver of(ServiceResolver resolver) {
        return new ScopedResolver(resolver);
    }
}
