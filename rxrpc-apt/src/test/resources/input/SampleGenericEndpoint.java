package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Completable;
import io.reactivex.Observable;

public interface SampleGenericEndpoint<T> {
    @RxRpcMethod
    public Observable<T> genericMethod(T data);

    @RxRpcMethod
    public Observable<SampleGenericData<T>> genericDataMethod(String request);

    @RxRpcMethod
    public Completable genericInputDataMethod(SampleGenericData<T> data);
}
