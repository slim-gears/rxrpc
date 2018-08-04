package com.slimgears.rxrpc.server;

public class RxRpc_DummyEndpointFactory implements EndpointDispatcherFactory {
    private final static MethodDispatcher<DummyEndpoint, String> echoMethod = (target, args) ->
            Publishers.toPublisher(target.echoMethod(args.get("msg", String.class)));

    @Override
    public EndpointDispatcher create(EndpointResolver resolver) {
        return EndpointDispatchers
                .builder(() -> resolver.resolve(DummyEndpoint.class))
                .method("echoMethod", echoMethod)
                .build();
    }
}
