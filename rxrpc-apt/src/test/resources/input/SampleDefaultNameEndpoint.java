package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;

@RxRpcEndpoint()
public interface SampleDefaultNameEndpoint {
    @RxRpcMethod
    public int method();
}
