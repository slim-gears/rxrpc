package com.slimgears.rxrpc.client;

import com.slimgears.rxrpc.core.api.JsonEngine;
import io.reactivex.Single;

import java.util.concurrent.Future;

public class DummyEndpointClient extends AbstractClient {
    public DummyEndpointClient(Future<RxClient.Session> session, JsonEngine jsonEngine) {
        super(session, jsonEngine);
    }

    public Single<String> echoMethod(String msg) {
        return invokeSingle(String.class, "echoMethod", arguments().put("msg", msg));
    }
}
