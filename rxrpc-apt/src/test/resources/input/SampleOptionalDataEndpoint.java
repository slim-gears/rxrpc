package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

@RxRpcEndpoint("sampleOptionalDataEndpoint")
public interface SampleOptionalDataEndpoint {
    @RxRpcMethod
    Observable<SampleOptionalData> optionalDataMethod();
}
