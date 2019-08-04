package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
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
    public int intMethod( SampleRequest request) {
        return invokeBlocking(InvocationInfo
            .builder(TypeToken.of(Integer.class))
            .method("sample-endpoint/intMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("request", request)
            .build());
    }

    @Override
    public Observable<SampleArray[]> arrayObservableMethod( SampleData sampleData) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(SampleArray[].class))
            .method("sample-endpoint/arrayObservableMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("sampleData", sampleData)
            .build());
    }

    @Override
    public Future<String> futureStringMethod( String msg, @Nullable SampleRequest request) {
        return invokeFuture(InvocationInfo
            .builder(TypeToken.of(String.class))
            .method("sample-endpoint/futureStringMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("msg", msg)
                .arg("request", request)
            .build());
    }

    @Override
    public Observable<Boolean> futureBooleanMethod() {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(Boolean.class))
            .method("sample-endpoint/futureBooleanMethod")
            .shared(false)
            .sharedReplayCount(0)
        .build());
    }

    @Override
    public Observable<SampleData> observableDataMethod( SampleRequest request) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(SampleData.class))
            .method("sample-endpoint/observableDataMethod")
            .shared(true)
            .sharedReplayCount(0)
            .arg("request", request)
            .build());
    }

}
