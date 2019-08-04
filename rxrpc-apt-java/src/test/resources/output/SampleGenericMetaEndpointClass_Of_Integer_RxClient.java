package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Observable;
import java.lang.Integer;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpointClass_Of_Integer
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaEndpointClass_Of_Integer_RxClient extends AbstractClient {
    public SampleGenericMetaEndpointClass_Of_Integer_RxClient(Session session) {
        super(session);
    }

    public Observable<Integer> genericMethod( Integer data) {
        return invokeObservable(InvocationInfo
            .builder(TypeToken.of(Integer.class))
            .method("sample-generic-meta-endpoint-class_of_integer/genericMethod")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

}
