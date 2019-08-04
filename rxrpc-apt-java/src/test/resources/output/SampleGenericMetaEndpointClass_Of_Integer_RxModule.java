package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.Integer;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpointClass_Of_Integer
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpointClass_Of_Integer_RxModule implements Module {
    private final static MethodDispatcher<SampleGenericMetaEndpointClass_Of_Integer, Integer> genericMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.genericMethod(
            args.get("data", TypeToken.of(Integer.class))));

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleGenericMetaEndpointClass_Of_Integer.class)
        .method("genericMethod", genericMethod, Integer.class)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sample-generic-meta-endpoint-class_of_integer", router);
    }
}
