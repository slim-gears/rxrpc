package com.slimgears.rxrpc.sample;

import com.google.common.collect.ImmutableMap;
import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import com.slimgears.rxrpc.sample.SampleMapData;
import io.reactivex.Observable;

import java.util.Map;
import java.util.concurrent.Future;

@RxRpcEndpoint
public interface SampleMapEndpoint {
    @RxRpcMethod
    public Observable<SampleMapData> mapDataMethod(Map<String, SampleMapData> arg);

    @RxRpcMethod
    public Observable<ImmutableMap<String, SampleMapData>> mapOfMapDataMethod();
}
