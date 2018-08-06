package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.annotations.RxRpcEndpoint;
import com.slimgears.rxrpc.core.annotations.RxRpcMethod;
import com.slimgears.rxrpc.core.util.ImmediateFuture;
import com.slimgears.rxrpc.server.MethodDispatcher;
import com.slimgears.rxrpc.server.Publishers;

import java.util.concurrent.Future;

@RxRpcEndpoint("sampleEndpoint")
public class SampleEndpoint {
    static MethodDispatcher<SampleEndpoint, String> echoMethod = (target, args) ->
            Publishers.toPublisher(target.echoMethod(args.get("msg", String.class)));

    @RxRpcMethod
    public Future<String> echoMethod(String msg) {
        return ImmediateFuture.of("Server received from client: " + msg);
    }
}
