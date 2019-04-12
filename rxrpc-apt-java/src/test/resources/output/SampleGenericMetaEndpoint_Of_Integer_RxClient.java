package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import com.slimgears.util.reflect.TypeToken;
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
    public Observable<Integer> genericMethod(Integer data) {
        return invokeObservable(
            TypeToken.of(Integer.class),
            "sampleGenericMetaEndpoint_of_Integer/genericMethod",
            arguments()
                .put("data", data));
    }

    @Override
    public Observable<SampleGenericData<Integer>> genericDataMethod(String request) {
        return invokeObservable(
            new TypeToken<SampleGenericData<Integer>>(){},
            "sampleGenericMetaEndpoint_of_Integer/genericDataMethod",
            arguments()
                .put("request", request));
    }

    @Override
    public Completable genericInputDataMethod(SampleGenericData<Integer> data) {
        return invokeCompletable(
            TypeToken.of(Void.class),
            "sampleGenericMetaEndpoint_of_Integer/genericInputDataMethod",
            arguments()
                .put("data", data));
    }

}
