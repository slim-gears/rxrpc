package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.Integer;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpoint_Of_Integer
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpoint_Of_Integer_RxModule implements Module {
    private final static MethodDispatcher<SampleGenericMetaEndpoint_Of_Integer, Integer> genericMethod = (resolver, target, args) ->
        Publishers.toPublisher( target.genericMethod(
            args.get("data", TypeToken.of(Integer.class))));

    private final static MethodDispatcher<SampleGenericMetaEndpoint_Of_Integer, SampleGenericData<Integer>> genericDataMethod = (resolver, target, args) ->
        Publishers.toPublisher( target.genericDataMethod(
            args.get("request", TypeToken.of(String.class))));

    private final static MethodDispatcher<SampleGenericMetaEndpoint_Of_Integer, Void> genericInputDataMethod = (resolver, target, args) ->
        Publishers.toPublisher( target.genericInputDataMethod(
            args.get("data", new TypeToken<SampleGenericData<Integer>>(){})));

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleGenericMetaEndpoint_Of_Integer.class)
        .method("genericMethod", genericMethod)
        .method("genericDataMethod", genericDataMethod)
        .method("genericInputDataMethod", genericInputDataMethod)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sampleGenericMetaEndpoint_of_Integer", router);
    }
}
