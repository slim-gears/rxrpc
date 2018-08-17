package com.slimgears.rxrpc;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.server.EndpointDispatcher;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.RxServer;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
    private Subject<String> serverSubject;

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
        serverSubject = ReplaySubject.create();

        EndpointDispatcher.Factory factory = EndpointDispatchers
                .builder(Object.class)
                .method("testMethod", (target, args) -> serverSubject
                        .map(s -> args.get("prefix", String.class) + ":" + s)
                        .toFlowable(BackpressureStrategy.BUFFER))
                .buildFactory();

        MockTransport mockTransport = new MockTransport();
        rxServer = RxServer
                .configBuilder()
                .server(mockTransport)
                .dispatcherFactory(factory)
                .createServer();

        rxClient = RxClient.forClient(mockTransport);

        rxServer.start();
    }

    @After
    public void tearDown() {
        rxServer.stop();
    }

    @Test
    public void testBasicClientServer() {
        TestObserver<String> tester = rxClient.connect(URI.create(""))
                .resolve(EndpointClient.class)
                .invokeObservable(String.class, "testMethod", args -> args.put("prefix", "[S]"))
                .test();

        Observable.just("One", "Two", "Three").subscribe(serverSubject);
        tester
                .awaitDone(1000, TimeUnit.MILLISECONDS)
                .assertValueCount(3)
                .assertValueAt(1, "[S]:Two");
    }

    @Test
    public void testClientUnsubscribeCausesServerUnsubscribe() {
        Disposable subscription = rxClient.connect(URI.create(""))
                .resolve(EndpointClient.class)
                .invokeObservable(String.class, "testMethod", args -> args.put("prefix", "[S]"))
                .subscribe();

        Assert.assertTrue(serverSubject.hasObservers());
        serverSubject.onNext("One");

        Assert.assertTrue(serverSubject.hasObservers());
        subscription.dispose();

        Assert.assertFalse(serverSubject.hasObservers());
    }
}
