package com.slimgears.rxrpc.sample;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.server.EndpointDispatcher.Configuration;
import com.slimgears.rxrpc.server.EndpointDispatcher.Factory;
import com.slimgears.rxrpc.server.EndpointDispatcher.Module;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.internal.MethodDispatcher;
import com.slimgears.rxrpc.server.internal.Publishers;
import java.lang.Integer;
import java.lang.String;

@AutoService(Module.class)
public class SampleEndpoint_RxModule implements Module {
    private final static MethodDispatcher<SampleEndpoint, String> futureStringMethod = (target, args) ->
            Publishers.toPublisher(target.futureStringMethod(
                    args.get("msg", String.class),
                    args.get("request", SampleRequest.class)));

    private final static MethodDispatcher<SampleEndpoint, Integer> intMethod = (target, args) ->
            Publishers.toPublisher(target.intMethod(
                    args.get("request", SampleRequest.class)));


    private final static Factory dispatcherFactory = EndpointDispatchers
            .builder(SampleEndpoint.class)
            .method("futureStringMethod", futureStringMethod)
            .method("intMethod", intMethod)
            .buildFactory();

    @Override
    public void configure(Configuration configuration) {
        configuration.addFactory("sampleEndpoint", dispatcherFactory);
    }
}
