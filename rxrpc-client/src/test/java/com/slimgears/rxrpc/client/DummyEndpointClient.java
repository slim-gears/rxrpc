package com.slimgears.rxrpc.client;

import io.reactivex.Single;

import java.util.concurrent.Future;

public class DummyEndpointClient extends AbstractClient {
    public DummyEndpointClient(Future<RxClient.Session> session) {
        super(session);
    }

    public Single<String> echoMethod(String msg) {
        return invokeSingle(String.class, "echoMethod", arguments().put("msg", msg));
    }
}
