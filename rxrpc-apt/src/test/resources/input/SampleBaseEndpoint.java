package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcMethod;

public interface SampleBaseEndpoint {
    @RxRpcMethod
    public int intMethod(SampleRequest request);
}