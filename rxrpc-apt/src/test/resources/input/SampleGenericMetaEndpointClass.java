package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import com.slimgears.rxrpc.core.RxRpcMethod;
import io.reactivex.Observable;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

@RxRpcGenerate(
        className = "SampleGenericMetaEndpointClass_Of_${T}",
        annotation = @RxRpcEndpoint(options = "rxrpc.java.server=true"),
        value = @RxRpcGenerate.Endpoint(params = Integer.class))
public class SampleGenericMetaEndpointClass<T> {
    private final Class<T> paramClass;
    private final List<T> items;

    @RxRpcMethod
    public Observable<T> genericMethod(T data) {
        return Observable.fromIterable(items);
    }

    @Inject
    protected SampleGenericMetaEndpointClass(@RxRpcGenerate.ClassParam("T") Class<T> paramClass,
                                             @Named("test") List<T> items) {
        this.paramClass = paramClass;
        this.items = items;
    }
}
