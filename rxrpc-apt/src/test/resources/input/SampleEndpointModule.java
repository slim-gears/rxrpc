package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcModule;
import com.slimgears.rxrpc.sample.SampleEndpoint;
import com.slimgears.rxrpc.server.EndpointDispatcher;
import com.slimgears.rxrpc.server.EndpointDispatcher.Module;

@RxRpcModule(name = "sample", endpointClass = SampleEndpoint.class)
public class SampleEndpointModule implements Module {
    @Override
    public void configure(EndpointDispatcher.Configuration configuration) {

    }
}