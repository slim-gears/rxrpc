package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.server.internal.CompositeEndpointRouter;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.util.generic.ServiceResolver;
import com.slimgears.util.stream.Safe;
import com.slimgears.util.stream.Streams;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.slimgears.util.stream.Streams.ofType;

public class EndpointRouters {
    private final static Logger log = LoggerFactory.getLogger(EndpointRouters.class);
    public final static EndpointRouter EMPTY = (path, args) -> { throw new NoSuchMethodError(path); };
    public final static EndpointRouter.Module EMPTY_MODULE = config -> {};

    public static <T> Builder<T> builder(Class<T> cls) {
        return Builder.create(cls);
    }

    public static EndpointRouter.Module modules(EndpointRouter.Module... modules) {
        return config -> Arrays.asList(modules).forEach(m -> m.configure(config));
    }

    public static EndpointRouter.Module moduleByName(String moduleName) {
        ClassLoader classLoader = EndpointRouters.class.getClassLoader();
        String resourcePath = "META-INF/rxrpc-modules/" + moduleName;
        try {
            EndpointRouter.Module[] modules = Streams.fromEnumeration(classLoader.getResources(resourcePath))
                    .flatMap(EndpointRouters::readClassNames)
                    .map(Safe.ofFunction(Class::forName))
                    .map(Safe.ofFunction(Class::newInstance))
                    .flatMap(ofType(EndpointRouter.Module.class))
                    .toArray(EndpointRouter.Module[]::new);
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

    public static EndpointRouter.Module discover() {
        ServiceLoader<EndpointRouter.Module> serviceLoader = ServiceLoader.load(
                EndpointRouter.Module.class, EndpointRouters.class.getClassLoader());

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

        public EndpointRouter.Factory buildFactory() {
            return resolver -> createEndpointRouter(() -> resolver.resolve(endpointClass));
        }

        private EndpointRouter createEndpointRouter(Supplier<T> targetSupplier) {
            return (path, args) -> Builder.this.dispatch(targetSupplier.get(), path, args);
        }

        private Publisher<?> dispatch(T target, String method, InvocationArguments args) {
            return Optional
                    .ofNullable(methodDispatcherMap.get(method))
                    .map(dispatcher -> dispatcher.dispatch(target, args))
                    .orElseThrow(() -> new NoSuchMethodError("Method " + method + " not found"));
        }
    }

    public static EndpointRouter.Factory factoryFromModules(EndpointRouter.Module... modules) {
        CompositeBuilder compositeBuilder = compositeBuilder();
        modules(modules).configure(compositeBuilder);
        return compositeBuilder.build();
    }

    private static CompositeBuilder compositeBuilder() {
        return new CompositeBuilder();
    }

    private static class CompositeBuilder implements EndpointRouter.Configuration {
        private final Map<String, EndpointRouter.Factory> dispatcherMap = new HashMap<>();

        public CompositeBuilder add(String path, EndpointRouter.Factory dispatcher) {
            dispatcherMap.put(path, dispatcher);
            return this;
        }

        public EndpointRouter build(ServiceResolver resolver) {
            return new CompositeEndpointRouter(resolver, dispatcherMap);
        }

        public EndpointRouter.Factory build() {
            return this::build;
        }

        @Override
        public void addFactory(String path, EndpointRouter.Factory factory) {
            add(path, factory);
        }
    }
}
