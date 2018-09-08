package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.server.internal.InvocationArguments;
import com.slimgears.util.generic.ServiceResolver;
import org.reactivestreams.Publisher;

public interface EndpointRouter {
    Publisher<?> dispatch(String path, InvocationArguments args);

    interface Factory {
        EndpointRouter create(ServiceResolver resolver);
    }

    interface Configuration {
        void addFactory(String path, EndpointRouter.Factory factory);
    }

    interface Module {
        void configure(Configuration configuration);
    }
}
