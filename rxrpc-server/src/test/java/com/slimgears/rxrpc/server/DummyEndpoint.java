package com.slimgears.rxrpc.server;

import com.slimgears.rxrpc.core.annotations.RxRpcEndpoint;
import com.slimgears.rxrpc.core.annotations.RxRpcMethod;
import io.reactivex.Single;

@RxRpcEndpoint("test")
public class DummyEndpoint {
    @RxRpcMethod
    public Single<String> echoMethod(String msg) {
        return Single.just("Server response: " + msg);
    }
}
