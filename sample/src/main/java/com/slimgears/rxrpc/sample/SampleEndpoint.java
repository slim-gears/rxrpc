/**
 *
 */
package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

import java.util.concurrent.Future;

@RxRpcEndpoint(module = "sampleModule")
public interface SampleEndpoint {
    @RxRpcMethod
    Future<String> futureStringMethod(String msg, SampleRequest request);

    @RxRpcMethod
    int blockingMethod(SampleRequest request);

    @RxRpcMethod
    Observable<SampleNotification> observableMethod(SampleRequest request);

    @RxRpcMethod
    Observable<SampleNotification> errorProducingMethod(String message);

    @RxRpcMethod
    int blockingErrorProducingMethod(String message);
}
