package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

import java.util.concurrent.Future;

@RxRpcEndpoint(module = "test", options = "rxrpc.java.server=true")
public interface SampleCircularReferenceEndpoint {
    @RxRpcMethod
    public Observable<SampleCircularReferenceData> observableDataMethod();
}
