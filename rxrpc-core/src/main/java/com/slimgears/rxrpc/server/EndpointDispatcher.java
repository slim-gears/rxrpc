package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.EndpointResolver;
import com.slimgears.rxrpc.server.internal.InvocationArguments;
import org.reactivestreams.Publisher;

public interface EndpointDispatcher {
    Publisher<?> dispatch(String path, InvocationArguments args);

    interface Factory {
        EndpointDispatcher create(EndpointResolver resolver);
    }

    interface Configuration {
        void addFactory(String path, EndpointDispatcher.Factory factory);
    }

    interface Module {
        void configure(Configuration configuration);
    }
}
