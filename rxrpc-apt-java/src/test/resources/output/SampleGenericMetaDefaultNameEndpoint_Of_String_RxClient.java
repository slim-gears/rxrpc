package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import java.lang.String;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaDefaultNameEndpoint_Of_String
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleGenericMetaDefaultNameEndpoint_Of_String_RxClient extends AbstractClient implements SampleGenericMetaDefaultNameEndpoint_Of_String {
    public SampleGenericMetaDefaultNameEndpoint_Of_String_RxClient(Session session) {
        super(session);
    }

    @Override
    public String method() {
        return invokeBlocking(InvocationInfo
            .builder(TypeToken.of(String.class))
            .method("sample-generic-meta-default-name-endpoint_of_string/method")
            .shared(false)
            .sharedReplayCount(0)
        .build());
    }

}
