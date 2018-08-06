package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient;

import java.util.concurrent.Future;

public class SampleEndpointClient extends AbstractClient  {
    public SampleEndpointClient(Future<RxClient.Session> session) {
        super(session);
    }

    public Future<String> echoMethod(String msg) {
        return invokeFuture(String.class,"echoMethod", arguments().put("msg", msg));
    }
}
