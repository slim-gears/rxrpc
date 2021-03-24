package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

@RxRpcEndpoint
public interface RepetitionSayHelloEndpoint {
    @RxRpcMethod
    Observable<String> sayHello(String name, int delayMillis, int count);
}
