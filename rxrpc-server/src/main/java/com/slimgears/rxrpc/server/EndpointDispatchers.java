package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.data.Path;
import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class EndpointDispatchers {
    public final static EndpointDispatcher EMPTY = (path, args) -> { throw new NoSuchMethodError(path); };

    public static <T> Builder<T> builder(Class<T> cls) {
        return Builder.create(cls);
    }

    public static EndpointDispatcher.Module modules(EndpointDispatcher.Module... modules) {
        return config -> Arrays.asList(modules).forEach(m -> m.configure(config));
    }

    public static EndpointDispatcher.Module discover() {
        ServiceLoader<EndpointDispatcher.Module> serviceLoader = ServiceLoader.load(EndpointDispatcher.Module.class);
        return config -> serviceLoader.forEach(module -> module.configure(config));
    }

    public static class Builder<T> {
        private final Map<String, MethodDispatcher<T, ?>> methodDispatcherMap = new HashMap<>();
        private final Class<T> endpointClass;

        private Builder(Class<T> endpointClass) {
            this.endpointClass = endpointClass;
        }

        public static <T> Builder<T> create(Class<T> endpointClass) {
            return new Builder<>(endpointClass);
        }

        public <R> Builder<T> method(String name, MethodDispatcher<T, R> dispatcher) {
            methodDispatcherMap.put(name, dispatcher);
            return this;
        }

        public EndpointDispatcher.Factory buildFactory() {
            return resolver -> createEndpointDispatcher(() -> resolver.resolve(endpointClass));
        }

        private EndpointDispatcher createEndpointDispatcher(Supplier<T> targetSupplier) {
            return (path, args) -> Builder.this.dispatch(targetSupplier.get(), path, args);
        }

        private Publisher<?> dispatch(T target, String method, InvocationArguments args) {
            return Optional
                    .ofNullable(methodDispatcherMap.get(method))
                    .map(dispatcher -> dispatcher.dispatch(target, args))
                    .orElseThrow(() -> new NoSuchMethodError("Method " + method + " not found"));
        }
    }

    public static EndpointDispatcher.Factory factoryFromModules(EndpointDispatcher.Module... modules) {
        CompositeBuilder compositeBuilder = compositeBuilder();
        modules(modules).configure(compositeBuilder);
        return compositeBuilder.build();
    }

    private static CompositeBuilder compositeBuilder() {
        return new CompositeBuilder();
    }

    private static class CompositeBuilder implements EndpointDispatcher.Configuration {
        private final Map<String, EndpointDispatcher.Factory> dispatcherMap = new HashMap<>();

        public CompositeBuilder add(String path, EndpointDispatcher.Factory dispatcher) {
            dispatcherMap.put(path, dispatcher);
            return this;
        }

        public EndpointDispatcher build(EndpointResolver resolver) {
            return new CompositeEndpointDispatcher(resolver, dispatcherMap);
        }

        public EndpointDispatcher.Factory build() {
            return this::build;
        }

        @Override
        public void addFactory(String path, EndpointDispatcher.Factory factory) {
            add(path, factory);
        }
    }
}
