/**
 *
 */
package com.slimgears.rxrpc.server.internal;

import com.slimgears.rxrpc.core.data.EndpointPath;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.RouteNotFoundException;
import com.slimgears.util.generic.ServiceResolver;
import org.reactivestreams.Publisher;

import java.util.Map;
import java.util.Optional;

import static com.slimgears.rxrpc.server.EndpointRouters.EMPTY;

public class CompositeEndpointRouter implements EndpointRouter {
    private final ServiceResolver resolver;
    private final Map<String, Factory> dispatcherMap;

    public CompositeEndpointRouter(ServiceResolver resolver, Map<String, Factory> dispatcherMap) {
        this.resolver = resolver;
        this.dispatcherMap = dispatcherMap;
    }

    @Override
    public Publisher<?> dispatch(String path, InvocationArguments args) {
        EndpointPath p = EndpointPath.of(path);
        return Optional
                .ofNullable(dispatcherMap.get(p.head()))
                .map(factory -> factory.create(resolver))
                .orElseThrow(() -> new RouteNotFoundException(path))
                .dispatch(p.tail(), args);
    }
}
