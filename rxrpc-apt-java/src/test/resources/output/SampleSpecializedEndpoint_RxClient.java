package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleSpecializedEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleSpecializedEndpoint_RxClient extends AbstractClient implements SampleSpecializedEndpoint {
    public SampleSpecializedEndpoint_RxClient(Session session) {
        super(session);
    }

    @Override
    public Observable<String> genericMethod( String data) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(String.class))
            .method("sampleSpecializedEndpoint/genericMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

    @Override
    public Observable<SampleGenericData<String>> genericDataMethod( String request) {
        return invokeObservable(InvocationInfo
            .builder(new TypeToken<SampleGenericData<String>>(){})
            .method("sampleSpecializedEndpoint/genericDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("request", request)
            .build());
    }

    @Override
    public Observable<SampleGenericList<String>> genericListMethod() {
        return invokeObservable(InvocationInfo
            .builder(new TypeToken<SampleGenericList<String>>(){})
            .method("sampleSpecializedEndpoint/genericListMethod")
            .shared(false)
            .sharedReplayCount(0)
        .build());
    }

    @Override
    public Completable genericInputDataMethod( SampleGenericData<String> data) {
        return invokeCompletable(InvocationInfo
            .builder(TypeToken.of(Void.class))
            .method("sampleSpecializedEndpoint/genericInputDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

    @Override
    public Observable<SampleSpecializedData> data() {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(SampleSpecializedData.class))
            .method("sampleSpecializedEndpoint/data")
            .shared(false)
            .sharedReplayCount(0)
        .build());
    }

}
