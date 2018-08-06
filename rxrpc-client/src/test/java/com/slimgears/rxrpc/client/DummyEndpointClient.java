package com.slimgears.rxrpc.client;

import com.slimgears.rxrpc.client.AbstractClient;
import java.util.concurrent.Future;
import io.reactivex.*;

public class DummyEndpointClient extends AbstractClient {
    public DummyEndpointClient(Future<RxClient.Session> session) {
        super(session);
    }

    @Override
    public Single<String> echoMethod(String msg) {
        return invokeSingle(String.class,        "echoMethod", arguments().put("msg", msg));
    }
}
