package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

@RxRpcEndpoint("sampleEndpoint")
public interface SampleDerivedDataEndpoint {
    @RxRpcMethod
    public Observable<SampleDerivedData> observableDataMethod();
}
