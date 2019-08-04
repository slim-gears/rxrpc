package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.core.RxRpcModule;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouter.Module;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
@RxRpcModule(name = "test", endpointClass = SampleEndpoint.class)
public class SampleEndpoint_RxModule implements Module {
    private final static MethodDispatcher<SampleEndpoint, Integer> intMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.intMethod(
            args.get("request", TypeToken.of(SampleRequest.class))));

    private final static MethodDispatcher<SampleEndpoint, SampleArray[]> arrayObservableMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.arrayObservableMethod(
            args.get("sampleData", TypeToken.of(SampleData.class))));

    private final static MethodDispatcher<SampleEndpoint, String> futureStringMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.futureStringMethod(
            args.get("msg", TypeToken.of(String.class)),
            args.get("request", TypeToken.of(SampleRequest.class))));

    private final static MethodDispatcher<SampleEndpoint, Boolean> futureBooleanMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.futureBooleanMethod());

    private final static MethodDispatcher<SampleEndpoint, SampleData> observableDataMethod = (resolver, target, args) ->
        Publishers.toPublisher(target.observableDataMethod(
            args.get("request", TypeToken.of(SampleRequest.class))));

    private final static EndpointRouter router = EndpointRouters
        .builder(SampleEndpoint.class)
        .method("intMethod", intMethod, SampleRequest.class)
        .method("arrayObservableMethod", arrayObservableMethod, SampleData.class)
        .method("futureStringMethod", futureStringMethod, String.class, SampleRequest.class)
        .method("futureBooleanMethod", futureBooleanMethod)
        .method("observableDataMethod", observableDataMethod, SampleRequest.class)
        .build();

    @Override
    public void configure(EndpointRouter.Configuration configuration) {
        configuration.addRouter("sample-endpoint", router);
    }
}
