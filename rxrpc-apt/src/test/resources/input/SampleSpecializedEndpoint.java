package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import com.slimgears.rxrpc.sample.SampleGenericEndpoint;
import com.slimgears.rxrpc.sample.SampleSpecializedData;
import io.reactivex.Observable;

@RxRpcEndpoint("sampleSpecializedEndpoint")
public interface SampleSpecializedEndpoint extends SampleGenericEndpoint<String> {
    @RxRpcMethod
    Observable<SampleSpecializedData> data();
}
