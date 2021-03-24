package com.slimgears.rxrpc.sample;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class RepetitionSayHelloEndpointImpl implements RepetitionSayHelloEndpoint {
    @Override
    public Observable<String> sayHello(String name, int delayMillis, int count) {
        return Observable.just("Hello, " + name).delay(delayMillis, TimeUnit.MILLISECONDS).repeat(count);
    }
}
