package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

public interface SampleGenericMetaEndpointInterface<T> {
    @RxRpcMethod
    Observable<T> genericMethod(T data);
}
