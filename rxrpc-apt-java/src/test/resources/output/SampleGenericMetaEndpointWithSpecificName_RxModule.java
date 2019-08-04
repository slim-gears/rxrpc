package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpointWithSpecificName
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpointWithSpecificName_RxModule implements Module {
    private final static MethodDispatcher<SampleGenericMetaEndpointWithSpecificName, String> genericMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericMethod(
            args.get("data", TypeToken.of(String.class))));

    private final static MethodDispatcher<SampleGenericMetaEndpointWithSpecificName, SampleGenericData<String>> genericDataMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericDataMethod(
            args.get("request", TypeToken.of(String.class))));

    private final static MethodDispatcher<SampleGenericMetaEndpointWithSpecificName, Void> genericInputDataMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericInputDataMethod(
            args.get("data", new TypeToken<SampleGenericData<String>>(){})));

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleGenericMetaEndpointWithSpecificName.class)
        .method("genericMethod", genericMethod, String.class)
        .method("genericDataMethod", genericDataMethod, String.class)
        .method("genericInputDataMethod", genericInputDataMethod, SampleGenericData.class)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sampleGenericMetaEndpointWithSpecificName", router);
    }
}
