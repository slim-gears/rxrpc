package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import com.slimgears.util.reflect.TypeToken;
import io.reactivex.Observable;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import java.util.concurrent.Future;
import javax.annotation.Generated;
import javax.annotation.Nullable;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleEndpoint_RxClient extends AbstractClient implements SampleEndpoint {
    public SampleEndpoint_RxClient(Session session) {
        super(session);
    }

    @Override
    public int intMethod(SampleRequest request) {
        return invokeBlocking(
            TypeToken.of(Integer.class),
            "sample-endpoint/intMethod",
            arguments()
                .put("request", request));
    }

    @Override
    public Observable<SampleArray[]> arrayObservableMethod(SampleData sampleData) {
        return invokeObservable(
            TypeToken.of(SampleArray[].class),
            "sample-endpoint/arrayObservableMethod",
            arguments()
                .put("sampleData", sampleData));
    }

    @Override
    public Future<String> futureStringMethod(String msg, @Nullable SampleRequest request) {
        return invokeFuture(
            TypeToken.of(String.class),
            "sample-endpoint/futureStringMethod",
            arguments()
                .put("msg", msg)
                .put("request", request));
    }

    @Override
    public Observable<Boolean> futureBooleanMethod() {
        return invokeObservable(
            TypeToken.of(Boolean.class),
            "sample-endpoint/futureBooleanMethod",
            arguments());
    }

    @Override
    public Observable<SampleData> observableDataMethod(SampleRequest request) {
        return invokeObservable(
            TypeToken.of(SampleData.class),
            "sample-endpoint/observableDataMethod",
            arguments()
                .put("request", request));
    }

}
