package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.RxDecorator;
import com.slimgears.rxrpc.core.RxRpcDecorator;
import com.slimgears.rxrpc.server.internal.CompositeEndpointRouter;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.util.generic.ServiceResolver;
import com.google.common.reflect.TypeToken;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
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
        private final Map<String, MethodDispatcher<T, ?>> methodDispatcherMap = new HashMap<>();
        private final Map<String, MethodDispatcher.Decorator> methodDecoratorMap = new HashMap<>();
        private final TypeToken<T> endpointType;

        private Builder(TypeToken<T> endpointType) {
            this.endpointType = endpointType;
        }

        public static <T> Builder<T> create(TypeToken<T> endpointType) {
            return new Builder<>(endpointType);
        }

        public <R> Builder<T> method(String name, MethodDispatcher<T, R> dispatcher, Class... args) {
            MethodDispatcher.Decorator decorator = DecoratorBuilder
                    .create(endpointType)
                    .method(name, args)
                    .build();

            methodDecoratorMap.put(name, decorator);
            methodDispatcherMap.put(name, dispatcher);
            return this;
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

    static class DecorationItem<A extends Annotation> {
        final Class<? extends RxDecorator<A>> decorator;
        final A annotation;

        private DecorationItem(Class<? extends RxDecorator<A>> decorator, A annotation) {
            this.decorator = decorator;
            this.annotation = annotation;
        }

        @SuppressWarnings("unchecked")
        static <A extends Annotation> DecorationItem<A> create(Class<? extends RxDecorator<? extends Annotation>> decoratorClass, Annotation annotation) {
            return new DecorationItem<>((Class<? extends RxDecorator<A>>)decoratorClass, (A)annotation);
        }
    }

    public static class DecoratorBuilder<T> {
        private final TypeToken<T> endpointType;
        private final List<DecorationItem<? extends Annotation>> decorationItems = new ArrayList<>();

        private DecoratorBuilder(TypeToken<T> endpointType) {
            this.endpointType = endpointType;
        }

        public static <T> DecoratorBuilder<T> create(TypeToken<T> endpointType) {
            return new DecoratorBuilder<>(endpointType);
        }

        public DecoratorBuilder<T> method(String name, Class... args) {
            try {
                Method method = endpointType.getRawType().getMethod(name, args);
                Arrays.stream(method.getAnnotations())
                        .flatMap(a -> Optional
                                .ofNullable(a.annotationType().getAnnotation(RxRpcDecorator.class))
                                .map(da -> DecorationItem.create(da.value(), a))
                                .map(Stream::of)
                                .orElseGet(Stream::empty))
                        .forEach(decorationItems::add);
                return this;
            } catch (NoSuchMethodException e) {
                return this;
            }
        }

        public MethodDispatcher.Decorator build() {
            return decorationItems.stream()
                    .map(this::buildDecorator)
                    .reduce(EndpointRouters::combineDecorators)
                    .orElse(emptyDecorator);
        }

        private <A extends Annotation> MethodDispatcher.Decorator buildDecorator(DecorationItem<A> item) {
            return new MethodDispatcher.Decorator() {
                @Override
                public <R> Publisher<R> decorate(Supplier<Publisher<R>> publisher, ServiceResolver resolver) {
                    RxDecorator<A> decorator = resolver.resolve(item.decorator);
                    return decorator.decorate(item.annotation, publisher);
                }
            };
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
