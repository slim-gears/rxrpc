package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

public interface SampleGenericEndpoint<T> {
    @RxRpcMethod
    public Observable<T> genericMethod(T data);
}
