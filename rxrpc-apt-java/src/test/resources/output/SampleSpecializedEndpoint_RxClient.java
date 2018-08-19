package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.lang.String;
import javax.annotation.Generated;

@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleSpecializedEndpoint_RxClient extends AbstractClient implements SampleSpecializedEndpoint {
    public SampleSpecializedEndpoint_RxClient(Single<Session> session) {
        super(session);
    }

    @Override
    public Observable<String> genericMethod(String data) {
        return invokeObservable(
                String.class,
                "sampleSpecializedEndpoint/genericMethod",
                arguments()
                        .put("data", data));
    }

}
