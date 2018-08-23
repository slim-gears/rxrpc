/**
 *
 */
package com.slimgears.rxrpc.core.util;

import com.slimgears.rxrpc.core.ServiceResolver;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServiceResolvers {
    public final static ServiceResolver defaultResolver = defaultConstructorResolver();

    public static ServiceResolver defaultConstructorResolver() {
        return new ServiceResolver() {
            @Override
            public <T> T resolve(Class<T> cls) {
                try {
                    if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
                        return null;
                    }
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
        private final Map<Class, Supplier> bindingMap = new HashMap<>();
        private ServiceResolver upstream = defaultResolver;

        public Builder parentResolver(ServiceResolver resolver) {
            upstream = resolver;
            return this;
        }

        public Builder apply(Consumer<Builder> config) {
            config.accept(this);
            return this;
        }

        public <T> BindingBuilder<T> bind(Class<T> cls) {
            return new BindingBuilder<>(cls);
        }

        public ServiceResolver build() {
            return new ServiceResolver() {
                @Override
                public <T> T resolve(Class<T> cls) {
                    //noinspection unchecked
                    return Optional
                            .ofNullable(bindingMap.get(cls))
                            .map(s -> (T)s.get())
                            .orElseGet(() -> upstream.resolve(cls));
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
                bindingMap.put(cls, supplier);
                return Builder.this;
            }

            public Builder to(Class<? extends T> cls) {
                return toSupplier(() -> upstream.resolve(cls));
            }
        }
    }
}
