package com.slimgears.rxrpc;

import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.RxServer;
import com.slimgears.util.generic.ServiceResolver;
import com.slimgears.util.reflect.TypeToken;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
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
 *
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

        public EndpointClient(RxClient.Session session) {
            super(session);
        }

        public <T> Observable<T> invokeObservable(TypeToken<T> responseType, String method, Function<InvocationArgs, InvocationArgs> args) {
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

        EndpointRouter router = EndpointRouters
                .builder(Object.class)
                .method("testMethod", (resolver, target, args) -> serverSubject
                        .map(s -> args.get("prefix", TypeToken.of(String.class)) + ":" + s)
                        .toFlowable(BackpressureStrategy.BUFFER), String.class)
                .build();

        MockTransport mockTransport = new MockTransport();
        rxServer = RxServer
                .configBuilder()
                .server(mockTransport)
                .router(router)
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
                .invokeObservable(TypeToken.of(String.class), "testMethod", args -> args.put("prefix", "[S]"))
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
                .invokeObservable(TypeToken.of(String.class), "testMethod", args -> args.put("prefix", "[S]"))
                .subscribe();

        Assert.assertTrue(serverSubject.hasObservers());
        serverSubject.onNext("One");

        Assert.assertTrue(serverSubject.hasObservers());
        subscription.dispose();

        Assert.assertFalse(serverSubject.hasObservers());
    }

    @Test
    public void testEndpointResolverClosesConnection() {
        try (ServiceResolver resolver = rxClient.connect(URI.create(""))) {
            resolver
                    .resolve(EndpointClient.class)
                    .invokeObservable(TypeToken.of(String.class), "testMethod", args -> args.put("prefix", "[S]"))
                    .subscribe();
            Assert.assertTrue(serverSubject.hasObservers());
        }

        Assert.assertFalse(serverSubject.hasObservers());
    }
}
