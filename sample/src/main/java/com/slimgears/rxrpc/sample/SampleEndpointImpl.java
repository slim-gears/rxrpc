package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class SampleEndpointImpl implements SampleEndpoint {
    public static class CustomException extends Exception {
        private final int customInt;
        private final String customString;
        private final double customDouble;

        public CustomException(String message, int customInt, String customString, double customDouble, Throwable cause) {
            super(message, cause);
            this.customInt = customInt;
            this.customString = customString;
            this.customDouble = customDouble;
        }

        @JsonProperty
        public int getCustomInt() {
            return customInt;
        }

        @JsonProperty
        public String customString() {
            return customString;
        }

        @JsonProperty("customDoubleProp")
        public double getCustomDouble() {
            return customDouble;
        }
    }

    @Override
    public Observable<String> sayHello(String name) {
        return Observable.interval(1000, TimeUnit.MILLISECONDS)
                .map(i -> "Hello, " + name + " #" + i);
    }

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
    public Observable<String> observeDecoratedMethod() {
        String name = SampleDecorator.Decorator.currentName();
        return Observable.fromCallable(() -> name);
    }

    @Override
    public Completable customErrorProducingMethod(String message) {
        return Completable.error(new CustomException(message, 23, "Test str", 29.7, null));
    }

    @Override
    public int blockingErrorProducingMethod(String message) {
        throw new IllegalStateException(message);
    }
}
