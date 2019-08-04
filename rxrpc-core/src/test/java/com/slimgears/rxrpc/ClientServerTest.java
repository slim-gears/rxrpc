package com.slimgears.rxrpc;

import com.google.common.collect.ImmutableMap;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.RxServer;
import com.slimgears.util.generic.ServiceResolver;
import com.google.common.reflect.TypeToken;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */

public class ClientServerTest {
    private final static Logger log = LoggerFactory.getLogger(ClientServerTest.class);

    private RxServer rxServer;
    private RxClient rxClient;
    private Subject<String> serverSubject;
    private MockTransport mockTransport;

    public static class EndpointClient extends AbstractClient {
        public EndpointClient(RxClient.Session session) {
            super(session);
        }

        public <T> Observable<T> invokeObservable(TypeToken<T> responseType, String method, Consumer<ImmutableMap.Builder<String, Object>> argConfig) {
            InvocationInfo.Builder<T> builder = InvocationInfo
                    .builder(responseType)
                    .method(method)
                    .shared(false)
                    .sharedReplayCount(0);
            argConfig.accept(builder.argsBuilder());
            return super.invokeObservable(builder.build());
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

        mockTransport = new MockTransport();
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
                .blockingGet()
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
                .blockingGet()
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
        try (ServiceResolver resolver = rxClient.connect(URI.create("")).blockingGet()) {
            resolver
                    .resolve(EndpointClient.class)
                    .invokeObservable(TypeToken.of(String.class), "testMethod", args -> args.put("prefix", "[S]"))
                    .subscribe();
            Assert.assertTrue(serverSubject.hasObservers());
        }

        Assert.assertFalse(serverSubject.hasObservers());
    }

    @Test
    public void testInvocationErrorCausesDisconnection() {
        AtomicBoolean complete = new AtomicBoolean();
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<String> value = new AtomicReference<>();
        Disposable subscription = rxClient.connect(URI.create(""))
                .blockingGet()
                .resolve(EndpointClient.class)
                .invokeObservable(TypeToken.of(String.class), "testMethod", args -> args.put("prefix", "[S]"))
                .subscribe(value::set, error::set, () -> complete.set(true));

        try {
            Assert.assertFalse(complete.get());
            Assert.assertNull(error.get());
            mockTransport.clientTransport().outgoing().onNext("Corrupted input");

            //Assert.assertTrue(complete.get());
            Assert.assertNotNull(error.get());
        } finally {
            subscription.dispose();
        }
    }
}
