package com.slimgears.rxrpc;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.server.EndpointDispatcher;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.RxServer;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */

public class ClientServerTest {
    private final static Logger log = LoggerFactory.getLogger(ClientServerTest.class);

    private RxServer rxServer;
    private RxClient rxClient;

    public static class EndpointClient extends AbstractClient {
        interface InvocationArgs {
            InvocationArgs put(String name, Object value);
        }

        public EndpointClient(Single<RxClient.Session> session) {
            super(session);
        }

        public <T> Observable<T> invokeObservable(Class<T> responseType, String method, Function<InvocationArgs, InvocationArgs> args) {
            InvocationArguments arguments = arguments();
            InvocationArgs invocationArgs = new InvocationArgs() {
                @Override
                public InvocationArgs put(String name, Object value) {
                    arguments.put(name, value);
                    return this;
                }
            };
            args.apply(invocationArgs);
            return super.invokeObservable(responseType, method, arguments);
        }
    }

    @Before
    public void setUp() {
        Observable<String> observable = Observable.just("One", "Two", "Three");

        EndpointDispatcher.Factory factory = EndpointDispatchers
                .builder(Object.class)
                .method("testMethod", (target, args) -> observable
                        .map(s -> args.get("prefix", String.class) + ":" + s)
                        .doOnNext(log::debug)
                        .toFlowable(BackpressureStrategy.BUFFER))
                .buildFactory();

        MockTransport mockTransport = new MockTransport();
        rxServer = RxServer
                .configBuilder()
                .server(mockTransport)
                .dispatcherFactory(factory)
                .createServer();

        rxClient = RxClient
                .configBuilder()
                .client(mockTransport)
                .createClient();

        rxServer.start();
    }

    @After
    public void tearDown() {
        rxServer.stop();
    }

    @Ignore
    @Test
    public void testClientServer() {
        rxServer.start();
        rxClient.connect(URI.create(""))
                .resolve(EndpointClient.class)
                .invokeObservable(String.class, "testMethod", args -> args.put("prefix", "[S]"))
                .doOnNext(log::debug)
                .test()
                .awaitDone(1000, TimeUnit.MILLISECONDS)
                .assertValueCount(3)
                .assertValueAt(1, "[S]:Two");
    }
}
