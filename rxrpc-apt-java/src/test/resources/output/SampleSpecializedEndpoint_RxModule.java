package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.server.EndpointDispatcher.Configuration;
import com.slimgears.rxrpc.server.EndpointDispatcher.Factory;
import com.slimgears.rxrpc.server.EndpointDispatcher.Module;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.String;
import javax.annotation.Generated;

@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleSpecializedEndpoint_RxModule implements Module {
    private final static MethodDispatcher<SampleSpecializedEndpoint, String> genericMethod = (target, args) ->
            Publishers.toPublisher(target.genericMethod(
                    args.get("data", String.class)));


    private final static Factory dispatcherFactory = EndpointDispatchers
            .builder(SampleSpecializedEndpoint.class)
            .method("genericMethod", genericMethod)
            .buildFactory();

    @Override
    public void configure(Configuration configuration) {
        configuration.addFactory("sampleSpecializedEndpoint", dispatcherFactory);
    }
}
