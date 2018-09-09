package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import com.slimgears.rxrpc.sample.SampleMetaEndpointInput;
import io.reactivex.Observable;
import java.util.List;

@RxRpcGenerate(
        className = "SampleMeta${T}Endpoint",
        annotation = @RxRpcEndpoint("sampleMeta${T}Endpoint"),
        value = @RxRpcGenerate.Endpoint(params = SampleMetaEndpointInput.class)
)
public interface SampleMetaEndpoint<T> {
    Observable<List<T>> data(String data);
}
