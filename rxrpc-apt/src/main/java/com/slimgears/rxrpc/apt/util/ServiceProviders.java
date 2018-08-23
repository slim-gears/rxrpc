/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.slimgears.rxrpc.core.util.Scope;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ServiceProviders {
    public static <T> Collection<T> loadServices(Class<T> cls) {
        return getServiceProvider().loadServices(cls);
    }

    private static ServiceProvider getServiceProvider() {
        return Scope.resolveOrDefault(ServiceProvider.class, ServiceProviders::loadServicesWithServiceLoader);
    }

    public static ServiceProvider ofMultiple(ServiceProvider... serviceProviders) {
        return new ServiceProvider() {
            @Override
            public <T> Collection<T> loadServices(Class<T> cls) {
                return Stream
                        .of(serviceProviders)
                        .flatMap(sp -> sp.loadServices(cls).stream())
                        .collect(Collectors.toList());
            }
        };
    }

    public static <T> Collection<T> loadWithServiceResolver(Class<T> cls) {
        return Optional.ofNullable(Scope.resolve(cls)).map(Collections::singleton).orElseGet(Collections::emptySet);
    }

    public static <T> Collection<T> loadServicesWithServiceLoader(Class<T> cls) {
        return StreamSupport
                .stream(ServiceLoader.load(cls, ServiceProviders.class.getClassLoader()).spliterator(), false)
                .collect(Collectors.toList());
    }
}
