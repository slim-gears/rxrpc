package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.Double;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpoint_Of_Double
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpoint_Of_Double_RxModule implements Module {
    private final static MethodDispatcher<SampleGenericMetaEndpoint_Of_Double, Double> genericMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericMethod(
            args.get("data", TypeToken.of(Double.class))));

    private final static MethodDispatcher<SampleGenericMetaEndpoint_Of_Double, SampleGenericData<Double>> genericDataMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericDataMethod(
            args.get("request", TypeToken.of(String.class))));

    private final static MethodDispatcher<SampleGenericMetaEndpoint_Of_Double, Void> genericInputDataMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericInputDataMethod(
            args.get("data", new TypeToken<SampleGenericData<Double>>(){})));

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleGenericMetaEndpoint_Of_Double.class)
        .method("genericMethod", genericMethod, Double.class)
        .method("genericDataMethod", genericDataMethod, String.class)
        .method("genericInputDataMethod", genericInputDataMethod, SampleGenericData.class)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sampleGenericMetaEndpoint_of_Double", router);
    }
}
