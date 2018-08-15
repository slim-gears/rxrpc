/**
 *
 */
package com.slimgears.rxrpc.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class EndpointResolvers {
    public static EndpointResolver defaultConstructorResolver() {
        return new EndpointResolver() {
            @Override
            public <T> T resolve(Class<T> cls) {
                try {
                    return cls.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Class, Supplier> resolverMap = new HashMap<>();
        private final AtomicReference<EndpointResolver> upstream = new AtomicReference<>(defaultConstructorResolver());

        public Builder parentResolver(EndpointResolver resolver) {
            upstream.set(resolver);
            return this;
        }

        public <T> BindingBuilder<T> bind(Class<T> cls) {
            return new BindingBuilder<>(cls);
        }

        public EndpointResolver build() {
            return new EndpointResolver() {
                @Override
                public <T> T resolve(Class<T> cls) {
                    //noinspection unchecked
                    return Optional
                            .ofNullable(resolverMap.get(cls))
                            .map(s -> (T)s.get())
                            .orElseGet(() -> upstream.get().resolve(cls));
                }
            };
        }

        public class BindingBuilder<T> {
            private final Class<T> cls;

            private BindingBuilder(Class<T> cls) {
                this.cls = cls;
            }

            public Builder toInstance(T instance) {
                return toSupplier(() -> instance);
            }

            public Builder toSupplier(Supplier<T> supplier) {
                resolverMap.put(cls, supplier);
                return Builder.this;
            }

            public Builder to(Class<? extends T> cls) {
                return toSupplier(() -> upstream.get().resolve(cls));
            }
        }
    }
}
