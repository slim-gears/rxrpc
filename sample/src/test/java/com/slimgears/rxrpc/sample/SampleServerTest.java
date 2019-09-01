package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.core.data.RxRpcRemoteException;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import com.slimgears.util.generic.ServiceResolver;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.containsString;

public class SampleServerTest {
    private final static int port = 11001;
    private final static URI uri = URI.create("ws://localhost:" + port + "/api/");
    private SampleServer server;
    private ServiceResolver clientResolver;

    @BeforeClass
    public static void init() {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.WARNING);
    }

    @Before
    public void setUp() throws Exception {
        server = new SampleServer(port);
        server.start();
        RxClient rxClient = RxClient.forClient(JettyWebSocketRxTransport.builder().buildClient());
        clientResolver = rxClient.connect(uri).blockingGet();
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
        String msgFromServer = sampleEndpointClient.futureStringMethod("Test", new SampleRequest(3, "sampleName", SampleRequest.class)).get();
        Assert.assertEquals("Server received from client: Test (id: 3, name: sampleName)", msgFromServer);
        int intFromServer = sampleEndpointClient.blockingMethod(new SampleRequest(4, "sampleName", SampleRequest.class));
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

    @Test
    public void testServerMethodReturnsCustomErrorAsync() throws InterruptedException {
        SampleEndpoint sampleEndpoint = clientResolver.resolve(SampleEndpoint_RxClient.class);
        sampleEndpoint
                .customErrorProducingMethod("Test error")
                .test()
                .await()
                .assertError(RxRpcRemoteException.class)
                .assertError(e -> ((RxRpcRemoteException)e).getErrorInfo().properties().containsKey("customInt"))
                .assertError(e -> ((RxRpcRemoteException)e).getErrorInfo().properties().containsKey("customDoubleProp"))
                .assertError(e -> ((RxRpcRemoteException)e).getErrorInfo().properties().containsKey("customString"))
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
                .echoData(new SampleMetaEndpoint.SampleData<>(new SampleRequest(1, "Alice", SampleRequest.class)))
                .map(data -> data.value.name)
                .test()
                .awaitDone(20, TimeUnit.SECONDS)
                .assertValueCount(2);
    }

    @Test
    public void testObjectMapper() throws IOException {
        SampleMetaEndpoint.SampleData<SampleRequest> data = new SampleMetaEndpoint.SampleData<>(new SampleRequest(3, "Bob", SampleRequest.class));
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);
        SampleMetaEndpoint.SampleData<SampleRequest> newData = objectMapper.readValue(
                json,
                new TypeReference<SampleMetaEndpoint.SampleData<SampleRequest>>(){});

        Assert.assertNotNull(newData);
        Assert.assertEquals(data.value.id, newData.value.id);
        Assert.assertEquals(data.value.name, newData.value.name);
    }

    @Test
    public void testDecoratedMethod() throws InterruptedException {
        SampleEndpoint sampleEndpoint = clientResolver.resolve(SampleEndpoint_RxClient.class);
        sampleEndpoint.observeDecoratedMethod()
                .test()
                .await()
                .assertValueCount(1)
                .assertValue("test1");
    }

    private String invokeHttpGet(String path) throws IOException {
        HttpURLConnection http = (HttpURLConnection)new URL("http://localhost:" + port)
                .openConnection();

        http.connect();
        Assert.assertEquals(HttpStatus.OK_200, http.getResponseCode());

        Object content = http.getContent();
        if (content instanceof InputStream) {
            return IOUtils.toString((InputStream)content, StandardCharsets.UTF_8);
        }
        return http.getResponseMessage();
    }

    @Test
    public void testStaticContentRetrieval() throws IOException {
        Assert.assertThat(invokeHttpGet(""), containsString("<app-root></app-root>"));
    }

    private void testObservableMethod(SampleEndpoint client, int count) {
        client
                .observableMethod(new SampleRequest(count, "Test", SampleRequest.class))
                .map(n -> n.data)
                .test()
                .awaitDone(20, TimeUnit.SECONDS)
                .assertComplete()
                .assertValueCount(count);
    }
}
