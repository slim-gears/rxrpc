package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.util.generic.ServiceResolver;
import org.reactivestreams.Publisher;

public interface EndpointRouter {
    Publisher<?> dispatch(ServiceResolver serviceResolver, String path, InvocationArguments args);

    interface Configuration {
        void addRouter(String path, EndpointRouter router);
    }

    interface Module {
        void configure(Configuration configuration);
    }
}
