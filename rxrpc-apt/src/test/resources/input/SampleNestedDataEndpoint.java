package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

@RxRpcEndpoint("sampleNestedDataEndpoint")
public interface SampleNestedDataEndpoint {
    class Data {
        enum Type {
            Type1,
            Type2
        }

        public Type type;
    }

    @RxRpcMethod
    public Observable<Data> observableDataMethod();
}
