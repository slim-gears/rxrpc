package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.annotations.RxRpcEndpoint;
import com.slimgears.rxrpc.core.annotations.RxRpcMethod;
import com.slimgears.rxrpc.core.util.ImmediateFuture;
import com.slimgears.rxrpc.server.MethodDispatcher;
import com.slimgears.rxrpc.server.Publishers;

import java.util.concurrent.Future;


@RxRpcEndpoint("sampleEndpoint")
public class SampleEndpoint {
    static MethodDispatcher<SampleEndpoint, String> futureStringMethod = (target, args) ->
            Publishers.toPublisher(target.futureStringMethod(
                    args.get("msg", String.class),
                    args.get("request", SampleRequest.class)));

    static MethodDispatcher<SampleEndpoint, Integer> intMethod = (target, args) ->
            Publishers.toPublisher(target.intMethod(
                    args.get("request", SampleRequest.class)));

    @RxRpcMethod
    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return ImmediateFuture.of(
                "Server received from client: " + msg + " (id: " + request.id + ", name: " + request.name + ")");
    }

    @RxRpcMethod
    public int intMethod(SampleRequest request) {
        return request.id + 1;
    }
}
