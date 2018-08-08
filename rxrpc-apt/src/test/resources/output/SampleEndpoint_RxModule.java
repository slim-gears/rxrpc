package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.server.EndpointDispatcher.Configuration;
import com.slimgears.rxrpc.server.EndpointDispatcher.Factory;
import com.slimgears.rxrpc.server.EndpointDispatcher.Module;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.MethodDispatcher;
import com.slimgears.rxrpc.server.Publishers;
import java.lang.String;

public class SampleEndpoint_RxModule implements Module {
    private static final MethodDispatcher<SampleEndpoint, String> futureStringMethod =
            (target, args) ->
                    Publishers.toPublisher(
                            target.futureStringMethod(
                                    args.get("msg", String), args.get("request", SampleRequest)));

    private static final MethodDispatcher<SampleEndpoint, Integer> intMethod =
            (target, args) ->
                    Publishers.toPublisher(target.intMethod(args.get("request", SampleRequest)));

    private static final Factory dispatcherFactory =
            EndpointDispatchers.builder(SampleEndpoint)
                    .method("futureStringMethod", futureStringMethod)
                    .method("intMethod", intMethod)
                    .buildFactory();

    @Override
    public void configure(Configuration configuration) {
        configuration.addFactory(sampleEndpoint, dispatcherFactory);
    }
}