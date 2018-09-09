package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Completable;
import io.reactivex.Observable;

@RxRpcGenerate(
        className = "SampleGenericMetaEndpoint_Of_${T}",
        annotation = @RxRpcEndpoint("sampleGenericMetaEndpoint_of_${T}"),
        value = {
                @RxRpcGenerate.Endpoint(
                        className = "SampleGenericMetaEndpointWithSpecificName",
                        annotation = @RxRpcEndpoint("sampleGenericMetaEndpointWithSpecificName"),
                        params = String.class),
                @RxRpcGenerate.Endpoint(
                        params = Integer.class),
                @RxRpcGenerate.Endpoint(
                        params = Double.class),
        })
public interface SampleGenericMetaEndpoint<T> {
    @RxRpcMethod
    public Observable<T> genericMethod(T data);

    @RxRpcMethod
    public Observable<SampleGenericData<T>> genericDataMethod(String request);

    @RxRpcMethod
    public Completable genericInputDataMethod(SampleGenericData<T> data);
}
