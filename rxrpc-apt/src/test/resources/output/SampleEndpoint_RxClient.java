package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Observable;
import java.lang.Integer;
import java.lang.String;
import java.util.concurrent.Future;
import javax.annotation.Generated;

@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleEndpoint_RxClient extends AbstractClient {
    public SampleEndpoint_RxClient(Future<Session> session) {
        super(session);
    }

    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return invokeFuture(
                String.class,
                "sampleEndpoint/futureStringMethod",
                arguments()
                        .put("msg", msg)
                        .put("request", request));
    }

    public int intMethod(SampleRequest request) {
        return invokeBlocking(
                Integer.class,
                "sampleEndpoint/intMethod",
                arguments()
                        .put("request", request));
    }

    public Observable<SampleData> observableDataMethod(SampleRequest request) {
        return invokeObservable(
                SampleData.class,
                "sampleEndpoint/observableDataMethod",
                arguments()
                        .put("request", request));
    }

}
