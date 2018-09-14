package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import io.reactivex.Observable;

@RxRpcGenerate(
        className = "SampleMetaEndpointImpl${T}",
        annotation = @RxRpcEndpoint(
                value = "sampleMeta${T}Endpoint",
                generateClient = false),
        value = {
                @RxRpcGenerate.Endpoint(params = Integer.class),
                @RxRpcGenerate.Endpoint(params = SampleRequest.class),
        })
public class SampleMetaEndpointImpl<T> implements SampleMetaEndpoint<T> {
    @Override
    public Observable<SampleData<T>> echoData(SampleData<T> data) {
        return Observable
                .just(new SampleData<>(data.value))
                .repeat(2);
    }
}
