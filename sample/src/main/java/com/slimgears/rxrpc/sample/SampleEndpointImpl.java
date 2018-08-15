package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcMethod;
import com.slimgears.rxrpc.core.util.ImmediateFuture;
import io.reactivex.Observable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class SampleEndpointImpl implements SampleEndpoint {
    @Override
    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return ImmediateFuture.of(
                "Server received from client: " + msg + " (id: " + request.id + ", name: " + request.name + ")");
    }

    @Override
    public int blockingMethod(SampleRequest request) {
        return request.id + 1;
    }

    @Override
    public Observable<SampleNotification> observableMethod(SampleRequest request) {
        return Observable
                .interval(0, 100, TimeUnit.MILLISECONDS)
                .take(request.id)
                .map(i -> new SampleNotification(request.name + " " + i, i));
    }
}
