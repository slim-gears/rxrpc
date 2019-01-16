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

public class CompositeEndpointRouter implements EndpointRouter {
    private final Map<String, EndpointRouter> routerMap;

    public CompositeEndpointRouter(Map<String, EndpointRouter> routerMap) {
        this.routerMap = routerMap;
    }

    @Override
    public Publisher<?> dispatch(ServiceResolver resolver, String path, InvocationArguments args) {
        EndpointPath p = EndpointPath.of(path);
        return Optional
                .ofNullable(routerMap.get(p.head()))
                .orElseThrow(() -> new RouteNotFoundException(path))
                .dispatch(resolver, p.tail(), args);
    }
}
