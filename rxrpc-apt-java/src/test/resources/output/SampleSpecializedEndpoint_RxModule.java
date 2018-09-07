package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.server.EndpointRouter.Configuration;
import com.slimgears.rxrpc.server.EndpointRouter.Factory;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleSpecializedEndpoint_RxModule implements Module {
    private final static MethodDispatcher<SampleSpecializedEndpoint, String> genericMethod = (target, args) ->
            Publishers.toPublisher(target.genericMethod(
                    args.get("data", String.class)));

    private final static MethodDispatcher<SampleSpecializedEndpoint, SampleGenericData<String>> genericDataMethod = (target, args) ->
            Publishers.toPublisher(target.genericDataMethod(
                    args.get("request", String.class)));

    private final static MethodDispatcher<SampleSpecializedEndpoint, Void> genericInputDataMethod = (target, args) ->
            Publishers.toPublisher(target.genericInputDataMethod(
                    args.get("data", (Class<SampleGenericData<String>>)(Class)SampleGenericData.class)));


    private final static Factory dispatcherFactory = EndpointRouters
            .builder(SampleSpecializedEndpoint.class)
            .method("genericMethod", genericMethod)
            .method("genericDataMethod", genericDataMethod)
            .method("genericInputDataMethod", genericInputDataMethod)
            .buildFactory();

    @Override
    public void configure(Configuration configuration) {
        configuration.addFactory("sampleSpecializedEndpoint", dispatcherFactory);
    }
}
