package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.jetty.http.JettyHttpRxTransportServer;
import com.slimgears.util.generic.ServiceResolver;
import io.reactivex.observers.BaseTestConsumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.net.ConnectException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHttpServerTest extends AbstractServerTest<JettyHttpRxTransportServer.Server> {

    @Override
    protected JettyHttpRxTransportServer.Server createServer() {
        return JettyHttpRxTransportServer.builder().build();
    }

    @Test
    public void testClientParallelism() {
        RxClient rxClient = RxClient.forClient(createClient());
        ServiceResolver clientResolverTwo = rxClient.connect(getUri()).timeout(1000, TimeUnit.MILLISECONDS).blockingGet();

        RepetitionSayHelloEndpoint sayHelloClientBob = clientResolver.resolve(RepetitionSayHelloEndpoint_RxClient.class);
        RepetitionSayHelloEndpoint sayHelloClientAlice = clientResolverTwo.resolve(RepetitionSayHelloEndpoint_RxClient.class);

        final int count = 10;
        final int periodMillis = 100;
        final int initialNotificationWaitMillis = periodMillis * 3;
        final int completeWaitMillis = count * periodMillis * 2;

        TestObserver<String> bobObserver = sayHelloClientBob.sayHello("Bob", periodMillis, count).subscribeOn(Schedulers.io()).test();
        TestObserver<String> aliceObserver = sayHelloClientAlice.sayHello("Alice", periodMillis, count).subscribeOn(Schedulers.io()).test();

        bobObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_10MS, initialNotificationWaitMillis);
        aliceObserver.awaitCount(1, BaseTestConsumer.TestWaitStrategy.SLEEP_10MS, initialNotificationWaitMillis);

        bobObserver.awaitDone(completeWaitMillis, TimeUnit.MILLISECONDS)
                .assertValueCount(count)
                .assertValueSet(Collections.nCopies(count, "Hello, Bob"));

        aliceObserver.awaitDone(completeWaitMillis, TimeUnit.MILLISECONDS)
                .assertValueCount(count)
                .assertValueSet(Collections.nCopies(count, "Hello, Alice"));

        clientResolverTwo.close();
    }

    @Test
    public void testServerDisconnection() throws Exception {
        SayHelloEndpoint sayHelloClient = clientResolver.resolve(SayHelloEndpoint_RxClient.class);
        server.stop();
        sayHelloClient
                .sayHello("Alice")
                .test()
                .await()
                .assertError(ConnectException.class);
    }
}
