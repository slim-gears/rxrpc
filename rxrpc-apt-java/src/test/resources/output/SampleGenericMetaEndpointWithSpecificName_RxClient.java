package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import com.slimgears.util.reflect.TypeToken;
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
    public Observable<String> genericMethod(String data) {
        return invokeObservable(
            TypeToken.of(String.class),
            "sampleGenericMetaEndpointWithSpecificName/genericMethod",
            arguments()
                .put("data", data));
    }

    @Override
    public Observable<SampleGenericData<String>> genericDataMethod(String request) {
        return invokeObservable(
            new TypeToken<SampleGenericData<String>>(){},
            "sampleGenericMetaEndpointWithSpecificName/genericDataMethod",
            arguments()
                .put("request", request));
    }

    @Override
    public Completable genericInputDataMethod(SampleGenericData<String> data) {
        return invokeCompletable(
            TypeToken.of(Void.class),
            "sampleGenericMetaEndpointWithSpecificName/genericInputDataMethod",
            arguments()
                .put("data", data));
    }

}
