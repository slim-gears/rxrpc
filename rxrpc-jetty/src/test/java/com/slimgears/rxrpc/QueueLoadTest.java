package com.slimgears.rxrpc;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.server.EndpointRouter;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.RxServer;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class QueueLoadTest {

    private RxClient rxClient;
    private RxServer rxServer;
    private Subject<String> serverSubject;
    private MockHttpTransport mockHttpTransport;

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

        mockHttpTransport = new MockHttpTransport();
        rxServer = RxServer
                .configBuilder()
                .server(mockHttpTransport.getServerTransport())
                .router(router)
                .createServer();

        rxClient = RxClient.forClient(mockHttpTransport.getClientTransport());

        rxServer.start();
    }

    @After
    public void tearDown() {
        rxServer.stop();
    }

    @Test
    public void testLoadTransportMessageQueue() {
        final int count = 500000;
        TestObserver<String> tester = rxClient.connect(URI.create(""))
                .blockingGet()
                .resolve(EndpointClient.class)
                .invokeObservable(TypeToken.of(String.class), "testMethod", args -> args.put("prefix", "[S]"))
                .test();

        Observable.range(0, count)
                .map(Object::toString)
                .subscribe(serverSubject);

        tester
                .awaitDone(240, TimeUnit.SECONDS)
                .assertValueCount(count);
    }
}
