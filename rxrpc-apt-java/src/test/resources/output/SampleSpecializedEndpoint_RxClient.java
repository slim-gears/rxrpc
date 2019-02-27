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
 * Generated from com.slimgears.rxrpc.sample.SampleSpecializedEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleSpecializedEndpoint_RxClient extends AbstractClient implements SampleSpecializedEndpoint {
    public SampleSpecializedEndpoint_RxClient(Session session) {
        super(session);
    }

    @Override
    public Observable<String> genericMethod(String data) {
        return invokeObservable(
                TypeToken.of(String.class),
                "sampleSpecializedEndpoint/genericMethod",
                arguments()
                        .put("data", data));
    }

    @Override
    public Observable<SampleGenericData<String>> genericDataMethod(String request) {
        return invokeObservable(
                new TypeToken<SampleGenericData<String>>(){},
                "sampleSpecializedEndpoint/genericDataMethod",
                arguments()
                        .put("request", request));
    }

    @Override
    public Observable<SampleGenericList<String>> genericListMethod() {
        return invokeObservable(
                new TypeToken<SampleGenericList<String>>(){},
                "sampleSpecializedEndpoint/genericListMethod",
                arguments());
    }

    @Override
    public Completable genericInputDataMethod(SampleGenericData<String> data) {
        return invokeCompletable(
                TypeToken.of(Void.class),
                "sampleSpecializedEndpoint/genericInputDataMethod",
                arguments()
                        .put("data", data));
    }

}
