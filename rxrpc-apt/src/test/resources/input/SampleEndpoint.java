package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

import java.util.concurrent.Future;

@RxRpcEndpoint(module = "test")
public interface SampleEndpoint extends SampleBaseEndpoint, SampleArrayEndpoint {
    @RxRpcMethod
    public Future<String> futureStringMethod(String msg, SampleRequest request);

    @RxRpcMethod
    public Observable<SampleData> observableDataMethod(SampleRequest request);
}
