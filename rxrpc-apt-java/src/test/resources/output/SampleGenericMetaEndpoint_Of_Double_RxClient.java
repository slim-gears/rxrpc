package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.lang.Double;
import java.lang.String;
import java.lang.Void;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpoint_Of_Double
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpoint_Of_Double_RxClient extends AbstractClient implements SampleGenericMetaEndpoint_Of_Double {
    public SampleGenericMetaEndpoint_Of_Double_RxClient(Session session) {
        super(session);
    }

    @Override
    public Observable<Double> genericMethod( Double data) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(Double.class))
            .method("sampleGenericMetaEndpoint_of_Double/genericMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

    @Override
    public Observable<SampleGenericData<Double>> genericDataMethod( String request) {
        return invokeObservable(InvocationInfo
            .builder(new TypeToken<SampleGenericData<Double>>(){})
            .method("sampleGenericMetaEndpoint_of_Double/genericDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("request", request)
            .build());
    }

    @Override
    public Completable genericInputDataMethod( SampleGenericData<Double> data) {
        return invokeCompletable(InvocationInfo
            .builder(TypeToken.of(Void.class))
            .method("sampleGenericMetaEndpoint_of_Double/genericInputDataMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

}
