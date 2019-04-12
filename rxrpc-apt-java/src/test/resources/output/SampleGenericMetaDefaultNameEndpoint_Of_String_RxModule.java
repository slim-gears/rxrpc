package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.String;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaDefaultNameEndpoint_Of_String
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaDefaultNameEndpoint_Of_String_RxModule implements Module {
    private final static MethodDispatcher<SampleGenericMetaDefaultNameEndpoint_Of_String, String> method = (resolver, target, args) ->
        Publishers.toPublisher(target.method());

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleGenericMetaDefaultNameEndpoint_Of_String.class)
        .method("method", method)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sample-generic-meta-default-name-endpoint_of_string", router);
    }
}
