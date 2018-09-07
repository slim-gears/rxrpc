package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcModule;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;

@RxRpcModule(name = "sample", endpointClass = SampleEndpoint.class)
public class SampleEndpointModule implements Module {
    @Override
    public void configure(EndpointRouter.Configuration configuration) {

    }
}