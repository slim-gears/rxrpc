package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.api.Lazy;
import com.slimgears.rxrpc.core.data.Path;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class EndpointDispatchers {
    public final static EndpointDispatcher EMPTY = (path, args) -> { throw new NoSuchMethodError(path); };

    public static <T> Builder<T> builder(Supplier<T> targetSupplier) {
        return Builder.create(targetSupplier);
    }

    public static <T> Builder<T> builder(EndpointResolver resolver, Class<T> cls) {
        return Builder.create(resolver, cls);
    }

    public static CompositeBuilder compositeBuilder() {
        return new CompositeBuilder();
    }

    public static class CompositeBuilder {
        private final Map<String, EndpointDispatcherFactory> dispatcherMap = new HashMap<>();

        public CompositeBuilder add(String path, EndpointDispatcherFactory dispatcher) {
            dispatcherMap.put(path, dispatcher);
            return this;
        }

        public EndpointDispatcher build(EndpointResolver resolver) {
            return (path, args) -> {
                Path p = Path.of(path);
                return Optional
                        .ofNullable(dispatcherMap.get(p.head()))
                        .map(factory -> factory.create(resolver))
                        .orElse(EMPTY)
                        .dispatch(p.tail(), args);
            };
        }

        public EndpointDispatcherFactory buildFactory() {
            return this::build;
        }
    }

    public static class Builder<T> {
        private final Map<String, MethodDispatcher<T, ?>> methodDispatcherMap = new HashMap<>();
        private final Lazy<T> target;

        private Builder(Supplier<T> targetSupplier) {
            this.target = Lazy.of(targetSupplier);
        }

        public static <T> Builder<T> create(Supplier<T> targetSupplier) {
            return new Builder<>(targetSupplier);
        }

        public static <T> Builder<T> create(EndpointResolver resolver, Class<T> endpointClass) {
            return create(() -> resolver.resolve(endpointClass));
        }

        public <R> Builder<T> method(String name, MethodDispatcher<T, R> dispatcher) {
            methodDispatcherMap.put(name, dispatcher);
            return this;
        }

        public EndpointDispatcher build() {
            return this::dispatch;
        }

        private Publisher<?> dispatch(String method, InvocationArguments args) {
            return Optional
                    .ofNullable(methodDispatcherMap.get(method))
                    .map(dispatcher -> dispatcher.dispatch(target.get(), args))
                    .orElseThrow(() -> new NoSuchMethodError("Method " + method + " not found"));
        }
    }
}
