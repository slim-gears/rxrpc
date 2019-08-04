package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.lang.Integer;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpoint_Of_Integer
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpoint_Of_Integer_RxClient extends AbstractClient implements SampleGenericMetaEndpoint_Of_Integer {
    public SampleGenericMetaEndpoint_Of_Integer_RxClient(Session session) {
        super(session);
    }

    @Override
    public Observable<Integer> genericMethod( Integer data) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(Integer.class))
            .method("sampleGenericMetaEndpoint_of_Integer/genericMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

    @Override
    public Observable<SampleGenericData<Integer>> genericDataMethod( String request) {
        return invokeObservable(InvocationInfo
            .builder(new TypeToken<SampleGenericData<Integer>>(){})
            .method("sampleGenericMetaEndpoint_of_Integer/genericDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("request", request)
            .build());
    }

    @Override
    public Completable genericInputDataMethod( SampleGenericData<Integer> data) {
        return invokeCompletable(InvocationInfo
            .builder(TypeToken.of(Void.class))
            .method("sampleGenericMetaEndpoint_of_Integer/genericInputDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

}
