package com.slimgears.rxrpc.sample;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.core.RxRpcModule;
import com.slimgears.rxrpc.server.EndpointRouter.Configuration;
import com.slimgears.rxrpc.server.EndpointRouter.Factory;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import com.slimgears.util.reflect.TypeToken;
import java.lang.Integer;
import java.lang.String;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
@RxRpcModule(name = "test", endpointClass = SampleEndpoint.class)
@AutoService(Module.class)
public class SampleEndpoint_RxModule implements Module {
    private final static MethodDispatcher<SampleEndpoint, Integer> intMethod = (target, args) ->
            Publishers.toPublisher(target.intMethod(
                    args.get("request", TypeToken.of(SampleRequest.class))));

    private final static MethodDispatcher<SampleEndpoint, SampleArray[]> arrayObservableMethod = (target, args) ->
            Publishers.toPublisher(target.arrayObservableMethod(
                    args.get("sampleData", TypeToken.of(SampleData.class))));

    private final static MethodDispatcher<SampleEndpoint, String> futureStringMethod = (target, args) ->
            Publishers.toPublisher(target.futureStringMethod(
                    args.get("msg", TypeToken.of(String.class)),
                    args.get("request", TypeToken.of(SampleRequest.class))));

    private final static MethodDispatcher<SampleEndpoint, SampleData> observableDataMethod = (target, args) ->
            Publishers.toPublisher(target.observableDataMethod(
                    args.get("request", TypeToken.of(SampleRequest.class))));

    private final static Factory dispatcherFactory = EndpointRouters
            .builder(SampleEndpoint.class)
            .method("intMethod", intMethod)
            .method("arrayObservableMethod", arrayObservableMethod)
            .method("futureStringMethod", futureStringMethod)
            .method("observableDataMethod", observableDataMethod)
            .buildFactory();

    @Override
    public void configure(Configuration configuration) {
        configuration.addFactory("sample-endpoint", dispatcherFactory);
    }
}
