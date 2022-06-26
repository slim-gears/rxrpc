package com.slimgears.rxrpc.server;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.core.RxDecorator;
import com.slimgears.rxrpc.core.RxRpcDecorator;
import com.slimgears.rxrpc.server.internal.CompositeEndpointRouter;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.util.generic.ServiceResolver;
import com.slimgears.util.reflect.ReflectUtils;
import com.slimgears.util.stream.Lazy;
import com.slimgears.util.stream.Safe;
import com.slimgears.util.stream.Streams;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.slimgears.util.stream.Streams.ofType;

@SuppressWarnings("WeakerAccess")
public class EndpointRouters {
    private final static Logger log = LoggerFactory.getLogger(EndpointRouters.class);
    public final static EndpointRouter empty = (resolver, path, args) -> { throw new NoSuchMethodError(path); };
    public final static EndpointRouter.Module emptyModule = config -> {};
    public final static MethodDispatcher.Decorator emptyDecorator = new MethodDispatcher.Decorator() {
        @Override
        public <R> Publisher<R> decorate(Supplier<Publisher<R>> publisher, ServiceResolver resolver) {
            return publisher.get();
        }
    };

    public static <T> Builder<T> builder(TypeToken<T> token) {
        return Builder.create(token);
    }
    public static <T> Builder<T> builder(Class<T> cls) {
        return Builder.create(TypeToken.of(cls));
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
            throw new RuntimeException("Could not read modules from " + resourcePath, e);
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

        return config -> serviceLoader.forEach(module -> {
            log.debug("Discovered module: {}", module.getClass().getSimpleName());
            module.configure(config);
        });
    }

    public static class Builder<T> {
        private final Lazy<Map<String, List<Method>>> methodsCache;
        private final Map<String, MethodDispatcher<T, ?>> methodDispatcherMap = new HashMap<>();
        private final Map<String, MethodDispatcher.Decorator> methodDecoratorMap = new HashMap<>();
        private final TypeToken<T> endpointType;

        private Builder(TypeToken<T> endpointType) {
            this.endpointType = endpointType;
            this.methodsCache = Lazy.of(() -> ReflectUtils.classHierarchy(endpointType.getRawType())
                    .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                    .collect(Collectors.groupingBy(Method::getName)));
        }

        public static <T> Builder<T> create(TypeToken<T> endpointType) {
            return new Builder<>(endpointType);
        }

        public <R> Builder<T> method(String name, MethodDispatcher<T, R> dispatcher) {
            methodDecoratorMap.put(name, decorate(name));
            methodDispatcherMap.put(name, dispatcher);
            return this;
        }

        private MethodDispatcher.Decorator decorate(String name) {
            var methods = methodsCache.get().getOrDefault(name, Collections.emptyList());
            return methods.stream()
                    .flatMap(m -> Arrays.stream(m.getAnnotations()))
                    .flatMap(a -> Optional
                            .ofNullable(a.annotationType().getAnnotation(RxRpcDecorator.class))
                            .map(da -> buildDecorator(da.value(), a)).stream())
                    .reduce(EndpointRouters::combineDecorators)
                    .orElse(emptyDecorator);
        }

        private <A extends Annotation> MethodDispatcher.Decorator buildDecorator(Class<? extends RxDecorator<? extends Annotation>> decoratorClass, A annotation) {
            return new MethodDispatcher.Decorator() {
                @Override
                public <R> Publisher<R> decorate(Supplier<Publisher<R>> publisher, ServiceResolver resolver) {
                    @SuppressWarnings("unchecked") RxDecorator<A> decorator = (RxDecorator<A>) resolver.resolve(decoratorClass);
                    return decorator.decorate(annotation, publisher);
                }
            };
        }
        public EndpointRouter build() {
            return Builder.this::dispatch;
        }

        @SuppressWarnings("unchecked")
        private <R> Publisher<R> dispatch(ServiceResolver resolver, String method, InvocationArguments args) {
            Supplier<Publisher<R>> publisherSupplier = Optional
                    .ofNullable(methodDispatcherMap.get(method))
                    .<Supplier<Publisher<R>>>map(dispatcher -> () -> (Publisher<R>)dispatcher.dispatch(resolver, resolver.resolve(endpointType), args))
                    .orElseThrow(() -> new NoSuchMethodError("Method " + method + " not found"));

            MethodDispatcher.Decorator decorator = Optional
                    .ofNullable(methodDecoratorMap.get(method))
                    .orElse(emptyDecorator);

            return decorator.decorate(publisherSupplier, resolver);
        }
    }

    public static EndpointRouter fromModules(EndpointRouter.Module... modules) {
        CompositeBuilder compositeBuilder = compositeBuilder();
        modules(modules).configure(compositeBuilder);
        return compositeBuilder.build();
    }

    private static CompositeBuilder compositeBuilder() {
        return new CompositeBuilder();
    }

    private static class CompositeBuilder implements EndpointRouter.Configuration {
        private final Map<String, EndpointRouter> dispatcherMap = new HashMap<>();

        public CompositeBuilder add(String path, EndpointRouter dispatcher) {
            dispatcherMap.put(path, dispatcher);
            return this;
        }

        public EndpointRouter build() {
            return new CompositeEndpointRouter(dispatcherMap);
        }

        @Override
        public void addRouter(String path, EndpointRouter router) {
            add(path, router);
        }
    }

    private static MethodDispatcher.Decorator combineDecorators(MethodDispatcher.Decorator first, MethodDispatcher.Decorator second) {
        return new MethodDispatcher.Decorator() {
            @Override
            public <R> Publisher<R> decorate(Supplier<Publisher<R>> publisher, ServiceResolver resolver) {
                return first.decorate(() -> second.decorate(publisher, resolver), resolver);
            }
        };
    }
}
