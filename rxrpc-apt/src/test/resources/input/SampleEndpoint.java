package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

import javax.annotation.Nullable;
import java.util.concurrent.Future;

@RxRpcEndpoint(module = "test", options = "rxrpc.java.server=true")
public interface SampleEndpoint extends SampleBaseEndpoint, SampleArrayEndpoint {
    @RxRpcMethod
    public Future<String> futureStringMethod(String msg, @Nullable SampleRequest request);

    @RxRpcMethod
    public Observable<Boolean> futureBooleanMethod();

    @RxRpcMethod(shared = true)
    public Observable<SampleData> observableDataMethod(SampleRequest request);
}
