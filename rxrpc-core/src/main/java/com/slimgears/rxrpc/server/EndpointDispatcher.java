package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.ServiceResolver;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import org.reactivestreams.Publisher;

public interface EndpointDispatcher {
    Publisher<?> dispatch(String path, InvocationArguments args);

    interface Factory {
        EndpointDispatcher create(ServiceResolver resolver);
    }

    interface Configuration {
        void addFactory(String path, EndpointDispatcher.Factory factory);
    }

    interface Module {
        void configure(Configuration configuration);
    }
}
