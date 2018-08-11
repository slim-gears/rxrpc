package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import com.slimgears.rxrpc.core.util.ImmediateFuture;
import io.reactivex.Observable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@RxRpcEndpoint("sampleEndpoint")
public class SampleEndpoint {
    @RxRpcMethod
    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return ImmediateFuture.of(
                "Server received from client: " + msg + " (id: " + request.id + ", name: " + request.name + ")");
    }

    @RxRpcMethod
    public int blockingMethod(SampleRequest request) {
        return request.id + 1;
    }

    @RxRpcMethod
    public Observable<SampleNotification> observableMethod(SampleRequest request) {
        return Observable
                .interval(0, 100, TimeUnit.MILLISECONDS)
                .take(request.id)
                .map(i -> new SampleNotification(request.name + " " + i, i));
    }
}
