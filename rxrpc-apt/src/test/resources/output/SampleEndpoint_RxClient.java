package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import java.lang.String;
import java.util.concurrent.Future;

public class SampleEndpoint_RxClient extends AbstractClient {
    public SampleEndpoint_RxClient(Future<Session> session) {
        super(session);
    }

    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return invokeFuture(
                java.lang.String.class,
                "sampleEndpoint/futureStringMethod",
                arguments().put("msg", msg).put("request", request));
    }

    public int intMethod(SampleRequest request) {
        return invokeBlocking(
                Integer.class, "sampleEndpoint/intMethod", arguments().put("request", request));
    }
}
