package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Observable;
import java.lang.Integer;
import java.lang.String;
import java.util.concurrent.Future;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleEndpoint_RxClient extends AbstractClient implements SampleEndpoint {
    public SampleEndpoint_RxClient(Session session) {
        super(session);
    }

    @Override
    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return invokeFuture(
                String.class,
                "sample-endpoint/futureStringMethod",
                arguments()
                        .put("msg", msg)
                        .put("request", request));
    }

    @Override
    public Observable<SampleData> observableDataMethod(SampleRequest request) {
        return invokeObservable(
                SampleData.class,
                "sample-endpoint/observableDataMethod",
                arguments()
                        .put("request", request));
    }

    @Override
    public int intMethod(SampleRequest request) {
        return invokeBlocking(
                Integer.class,
                "sample-endpoint/intMethod",
                arguments()
                        .put("request", request));
    }

    @Override
    public Observable<SampleArray[]> arrayObservableMethod(SampleData sampleData) {
        return invokeObservable(
                SampleArray[].class,
                "sample-endpoint/arrayObservableMethod",
                arguments()
                        .put("sampleData", sampleData));
    }

}
