package com.slimgears.rxrpc.sample;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class SampleEndpointImpl implements SampleEndpoint {
    @Override
    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return Single
                .just("Server received from client: " + msg + " (id: " + request.id + ", name: " + request.name + ")")
                .toFuture();
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

    @Override
    public Observable<SampleNotification> errorProducingMethod(String message) {
        return Observable.error(new IllegalStateException(message));
    }

    @Override
    public int blockingErrorProducingMethod(String message) {
        throw new IllegalStateException(message);
    }
}
