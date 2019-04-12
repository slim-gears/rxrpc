package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import com.slimgears.util.reflect.TypeToken;
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
    public Observable<Double> genericMethod(Double data) {
        return invokeObservable(
            TypeToken.of(Double.class),
            "sampleGenericMetaEndpoint_of_Double/genericMethod",
            arguments()
                .put("data", data));
    }

    @Override
    public Observable<SampleGenericData<Double>> genericDataMethod(String request) {
        return invokeObservable(
            new TypeToken<SampleGenericData<Double>>(){},
            "sampleGenericMetaEndpoint_of_Double/genericDataMethod",
            arguments()
                .put("request", request));
    }

    @Override
    public Completable genericInputDataMethod(SampleGenericData<Double> data) {
        return invokeCompletable(
            TypeToken.of(Void.class),
            "sampleGenericMetaEndpoint_of_Double/genericInputDataMethod",
            arguments()
                .put("data", data));
    }

}
