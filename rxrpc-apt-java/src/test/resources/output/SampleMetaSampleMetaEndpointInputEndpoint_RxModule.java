package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.String;
import java.util.List;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMetaSampleMetaEndpointInputEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleMetaSampleMetaEndpointInputEndpoint_RxModule implements Module {
    private final static MethodDispatcher<SampleMetaSampleMetaEndpointInputEndpoint, List<SampleMetaEndpointInput>> data = (resolver, target, args) ->
        Publishers.toPublisher(target.data(
            args.get("data", TypeToken.of(String.class))));

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleMetaSampleMetaEndpointInputEndpoint.class)
        .method("data", data, String.class)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sampleMetaSampleMetaEndpointInputEndpoint", router);
    }
}
