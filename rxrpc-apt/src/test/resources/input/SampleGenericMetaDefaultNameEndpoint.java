package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcGenerate;
import com.slimgears.rxrpc.core.RxRpcMethod;

@RxRpcGenerate(
        className = "SampleGenericMetaDefaultNameEndpoint_Of_${T}",
        value = @RxRpcGenerate.Endpoint(params = String.class))
public interface SampleGenericMetaDefaultNameEndpoint<T> {
    @RxRpcMethod
    public T method();
}
