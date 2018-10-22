package com.slimgears.rxrpc.sample;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import com.slimgears.util.generic.ServiceResolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class SampleServerTest {
    private final static int port = 9000;
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
                .awaitDone(20, TimeUnit.SECONDS)
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

    @Test
    public void testMetaEndpoint() {
        SampleMetaRequestEndpoint integerEndpoint = clientResolver.resolve(SampleMetaRequestEndpoint_RxClient.class);
        integerEndpoint
                .echoData(new SampleMetaEndpoint.SampleData<>(new SampleRequest(1, "Alice")))
                .map(data -> data.value.name)
                .test()
                .awaitDone(20, TimeUnit.SECONDS)
                .assertValueCount(2);
    }

    @Test
    public void testObjectMapper() throws IOException {
        SampleMetaEndpoint.SampleData<SampleRequest> data = new SampleMetaEndpoint.SampleData<>(new SampleRequest(3, "Bob"));
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);
        SampleMetaEndpoint.SampleData<SampleRequest> newData = objectMapper.readValue(
                json,
                new TypeReference<SampleMetaEndpoint.SampleData<SampleRequest>>(){});

        Assert.assertNotNull(newData);
        Assert.assertEquals(data.value.id, newData.value.id);
        Assert.assertEquals(data.value.name, newData.value.name);
    }

    private void testObservableMethod(SampleEndpoint client, int count) {
        client
                .observableMethod(new SampleRequest(count, "Test"))
                .map(n -> n.data)
                .test()
                .awaitDone(20, TimeUnit.SECONDS)
                .assertComplete()
                .assertValueCount(count);
    }
}
