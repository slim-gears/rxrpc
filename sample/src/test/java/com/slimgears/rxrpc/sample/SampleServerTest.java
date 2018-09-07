package com.slimgears.rxrpc.sample;

import ch.qos.logback.classic.Level;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.core.ServiceResolver;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class SampleServerTest {
    private final static int port = 8000;
    private final static URI uri = URI.create("ws://localhost:" + port + "/api/");

    @BeforeClass
    public static void init() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    @Test
    public void testSayHello() throws Exception {
        SampleServer server = new SampleServer(port);
        server.start();

        RxClient rxClient = RxClient.forClient(JettyWebSocketRxTransport.builder().buildClient());
        try (ServiceResolver resolver = rxClient.connect(uri)) {
            SayHelloEndpoint sayHelloClient = resolver.resolve(SayHelloEndpoint_RxClient.class);
            sayHelloClient
                    .sayHello("Alice")
                    .test()
                    .awaitDone(5000, TimeUnit.MILLISECONDS)
                    .assertValueCount(1)
                    .assertValue("Hello, Alice");
        } finally {
            server.stop();
        }
    }

    @Test
    public void testClientServer() throws Exception {
        SampleServer server = new SampleServer(port);
        server.start();

        RxClient rxClient = RxClient.forClient(JettyWebSocketRxTransport.builder().buildClient());

        try (ServiceResolver resolver = rxClient.connect(uri)) {
            SampleEndpoint sampleEndpointClient = resolver.resolve(SampleEndpoint_RxClient.class);
            String msgFromServer = sampleEndpointClient.futureStringMethod("Test", new SampleRequest(3, "sampleName")).get();
            Assert.assertEquals("Server received from client: Test (id: 3, name: sampleName)", msgFromServer);
            int intFromServer = sampleEndpointClient.blockingMethod(new SampleRequest(4, "sampleName"));
            Assert.assertEquals(5, intFromServer);
            sampleEndpointClient
                    .observableMethod(new SampleRequest(5, "Test"))
                    .map(n -> n.data)
                    .test()
                    .awaitDone(5000, TimeUnit.MILLISECONDS)
                    .assertComplete()
                    .assertValueCount(5);
        } finally {
            server.stop();
        }
    }
}
