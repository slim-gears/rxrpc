package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

public interface SampleArrayEndpoint {
    @RxRpcMethod
    public Observable<SampleArray[]> arrayObservableMethod(SampleData sampleData);
}