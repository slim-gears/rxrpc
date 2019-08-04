package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import java.lang.Integer;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleDefaultNameEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleDefaultNameEndpoint_RxClient extends AbstractClient implements SampleDefaultNameEndpoint {
    public SampleDefaultNameEndpoint_RxClient(Session session) {
        super(session);
    }

    @Override
    public int method() {
        return invokeBlocking(InvocationInfo
            .builder(TypeToken.of(Integer.class))
            .method("sample-default-name-endpoint/method")
            .shared(false)
            .sharedReplayCount(0)
        .build());
    }

}
