package com.slimgears.rxrpc.sample;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class SayHelloEndpointImpl implements SayHelloEndpoint {
    @Override
    public Observable<String> sayHello(String name) {
        return Observable.just("Hello, " + name).delay(500, TimeUnit.MILLISECONDS);
    }
}
