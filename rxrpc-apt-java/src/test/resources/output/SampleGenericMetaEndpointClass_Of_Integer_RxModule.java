package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.server.EndpointRouter.Configuration;
import com.slimgears.rxrpc.server.EndpointRouter.Factory;
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
    private final static MethodDispatcher<SampleGenericMetaEndpointClass_Of_Integer, Integer> genericMethod = (target, args) ->
            Publishers.toPublisher(target.genericMethod(
                    args.get("data", Integer.class)));

    private final static Factory dispatcherFactory = EndpointRouters
            .builder(SampleGenericMetaEndpointClass_Of_Integer.class)
            .method("genericMethod", genericMethod)
            .buildFactory();

    @Override
    public void configure(Configuration configuration) {
        configuration.addFactory("sample-generic-meta-endpoint-class_of_integer", dispatcherFactory);
    }
}
