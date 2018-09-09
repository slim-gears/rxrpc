package com.slimgears.rxrpc.sample;

import ch.qos.logback.classic.Level;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import com.slimgears.util.generic.ServiceResolver;
import io.reactivex.internal.functions.Functions;
import org.junit.*;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SampleServerTest {
    private final static int port = 8000;
    private final static URI uri = URI.create("ws://localhost:" + port + "/api/");
    private SampleServer server;
    private ServiceResolver clientResolver;

    @BeforeClass
    public static void init() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    @Before
    public void setUp() throws Exception {
        server = new SampleServer(port);
        server.start();
        RxClient rxClient = RxClient.forClient(JettyWebSocketRxTransport.builder().buildClient());
        clientResolver = rxClient.connect(uri);
    }

    @After
    public void tearDown() throws Exception {
        clientResolver.close();
        server.stop();
    }

    @Test
    public void testSayHello() {
        SayHelloEndpoint sayHelloClient = clientResolver.resolve(SayHelloEndpoint_RxClient.class);
        sayHelloClient
                .sayHello("Alice")
                .test()
                .awaitDone(5000, TimeUnit.MILLISECONDS)
                .assertValueCount(1)
                .assertValue("Hello, Alice");
    }

    @Test
    public void testClientServer() throws Exception {
        SampleEndpoint sampleEndpointClient = clientResolver.resolve(SampleEndpoint_RxClient.class);
        String msgFromServer = sampleEndpointClient.futureStringMethod("Test", new SampleRequest(3, "sampleName")).get();
        Assert.assertEquals("Server received from client: Test (id: 3, name: sampleName)", msgFromServer);
        int intFromServer = sampleEndpointClient.blockingMethod(new SampleRequest(4, "sampleName"));
        Assert.assertEquals(5, intFromServer);
        testObservableMethod(sampleEndpointClient, 5);
    }

    @Test
    public void testServerMethodReturnsErrorAsync() throws InterruptedException {
        SampleEndpoint sampleEndpoint = clientResolver.resolve(SampleEndpoint_RxClient.class);
        sampleEndpoint
                .errorProducingMethod("Test error")
                .test()
                .await()
                .assertError(IllegalStateException.class)
                .assertErrorMessage("Test error");
        testObservableMethod(sampleEndpoint, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void testServerMethodReturnsErrorBlocking() {
        SampleEndpoint sampleEndpoint = clientResolver.resolve(SampleEndpoint_RxClient.class);
        sampleEndpoint.blockingErrorProducingMethod("Test error");
        testObservableMethod(sampleEndpoint, 1);
    }

    private void testObservableMethod(SampleEndpoint client, int count) {
        client
                .observableMethod(new SampleRequest(count, "Test"))
                .map(n -> n.data)
                .test()
                .awaitDone(count * 1000 + 1000, TimeUnit.MILLISECONDS)
                .assertComplete()
                .assertValueCount(count);
    }
}
