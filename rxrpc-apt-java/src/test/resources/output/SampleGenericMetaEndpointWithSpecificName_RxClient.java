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
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpointWithSpecificName
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpointWithSpecificName_RxClient extends AbstractClient implements SampleGenericMetaEndpointWithSpecificName {
    public SampleGenericMetaEndpointWithSpecificName_RxClient(Session session) {
        super(session);
    }

    @Override
    public Observable<String> genericMethod( String data) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(String.class))
            .method("sampleGenericMetaEndpointWithSpecificName/genericMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

    @Override
    public Observable<SampleGenericData<String>> genericDataMethod( String request) {
        return invokeObservable(InvocationInfo
            .builder(new TypeToken<SampleGenericData<String>>(){})
            .method("sampleGenericMetaEndpointWithSpecificName/genericDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("request", request)
            .build());
    }

    @Override
    public Completable genericInputDataMethod( SampleGenericData<String> data) {
        return invokeCompletable(InvocationInfo
            .builder(TypeToken.of(Void.class))
            .method("sampleGenericMetaEndpointWithSpecificName/genericInputDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

}
