package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.ServiceResolver;
import com.slimgears.rxrpc.server.internal.CompositeEndpointDispatcher;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.util.stream.Safe;
import com.slimgears.util.stream.Streams;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.slimgears.util.stream.Streams.ofType;

public class EndpointDispatchers {
    private final static Logger log = LoggerFactory.getLogger(EndpointDispatchers.class);
    public final static EndpointDispatcher EMPTY = (path, args) -> { throw new NoSuchMethodError(path); };
    public final static EndpointDispatcher.Module EMPTY_MODULE = config -> {};

    public static <T> Builder<T> builder(Class<T> cls) {
        return Builder.create(cls);
    }

    public static EndpointDispatcher.Module modules(EndpointDispatcher.Module... modules) {
        return config -> Arrays.asList(modules).forEach(m -> m.configure(config));
    }

    public static EndpointDispatcher.Module moduleByName(String moduleName) {
        ClassLoader classLoader = EndpointDispatchers.class.getClassLoader();
        String resourcePath = "META-INF/rxrpc-modules/" + moduleName;
        try {
            EndpointDispatcher.Module[] modules = Streams.fromEnumeration(classLoader.getResources(resourcePath))
                    .flatMap(EndpointDispatchers::readClassNames)
                    .map(Safe.ofFunction(Class::forName))
                    .map(Safe.ofFunction(Class::newInstance))
                    .flatMap(ofType(EndpointDispatcher.Module.class))
                    .toArray(EndpointDispatcher.Module[]::new);
            return modules(modules);
        } catch (IOException e) {
            log.warn("Could not read modules from {}: {}", resourcePath, e);
            return EMPTY_MODULE;
        }
    }

    private static Stream<String> readClassNames(URL url) {
        try {
            InputStream stream = url.openStream();
            Reader reader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            return Streams
                    .takeWhile(Stream.generate(Safe.ofSupplier(bufferedReader::readLine)), Objects::nonNull)
                    .onClose(Safe.ofRunnable(stream::close));
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    public static EndpointDispatcher.Module discover() {
        ServiceLoader<EndpointDispatcher.Module> serviceLoader = ServiceLoader.load(
                EndpointDispatcher.Module.class, EndpointDispatchers.class.getClassLoader());

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

        public EndpointDispatcher build(ServiceResolver resolver) {
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
