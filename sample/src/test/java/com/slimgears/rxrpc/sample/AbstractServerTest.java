package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.data.RxRpcRemoteException;
import com.slimgears.util.generic.ServiceResolver;
import io.reactivex.observers.TestObserver;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.junit.*;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.containsString;

public abstract class AbstractServerTest<T extends RxTransport.Server & Servlet> {
    protected static AtomicInteger port = new AtomicInteger(11001);
    SampleServer<T> server;
    ServiceResolver clientResolver;

    @BeforeClass
    public static void init() {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.WARNING);
    }

    @Before
    public void setUp() throws Exception {
        server = SampleServer.forTransport(port.incrementAndGet(), createServer(), getTransportType());
        server.start();
        RxClient rxClient = RxClient.forClient(createClient());
        clientResolver = rxClient.connect(getUri()).timeout(1000, TimeUnit.MILLISECONDS).blockingGet();
    }

    @After
    public void tearDown() throws Exception {
        Optional.ofNullable(clientResolver).ifPresent(ServiceResolver::close);
        server.stop();
    }

    protected abstract RxTransport.Client createClient();
    protected abstract T createServer();
    protected abstract String getTransportType();

    protected String getUriScheme() {
        return getTransportType() + "://";
    }

    protected URI getUri() {
        return URI.create(getUriScheme() + "localhost:" + port.get() + "/api/");
    }

    @Test
    public void testStressServer() {
        final int clientCount = 150;
        final int messageCount = 10;
        final int periodMillis = 100;
        final int completeWaitMillis = messageCount * periodMillis * 2;

        Collection<ServiceResolver> clientResolvers = new LinkedList<>();
        Collection<TestObserver<String>> clientTestObservers = new LinkedList<>();

        for (int i = 0; i < clientCount; i++) {
            ServiceResolver clientResolver = RxClient
                    .forClient(createClient())
                    .connect(getUri())
                    .timeout(1000, TimeUnit.MILLISECONDS)
                    .blockingGet();
            clientResolvers.add(clientResolver);
        }

        clientResolvers.forEach(clientResolver -> {
            TestObserver<String> clientObserver = clientResolver.resolve(RepetitionSayHelloEndpoint_RxClient.class)
                    .sayHello("Bob", periodMillis, messageCount).test();
            clientTestObservers.add(clientObserver);
        });

        clientTestObservers.forEach(clientObserver -> {
            clientObserver.awaitDone(completeWaitMillis, TimeUnit.MILLISECONDS)
                    .assertValueCount(messageCount);
        });

        clientResolvers.forEach(ServiceResolver::close);
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
                .assertError(e -> Objects.requireNonNull(((RxRpcRemoteException) e).getErrorInfo().properties()).containsKey("customInt"))
                .assertError(e -> Objects.requireNonNull(((RxRpcRemoteException) e).getErrorInfo().properties()).containsKey("customDoubleProp"))
                .assertError(e -> Objects.requireNonNull(((RxRpcRemoteException) e).getErrorInfo().properties()).containsKey("customString"))
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
                new TypeReference<SampleMetaEndpoint.SampleData<SampleRequest>>() {
                });

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
        HttpURLConnection http = (HttpURLConnection) new URL("http://localhost:" + port.get())
                .openConnection();

        http.connect();
        Assert.assertEquals(HttpStatus.OK_200, http.getResponseCode());

        Object content = http.getContent();
        if (content instanceof InputStream) {
            return IOUtils.toString((InputStream) content, StandardCharsets.UTF_8);
        }
        return http.getResponseMessage();
    }

    @Test
    public void testStaticContentRetrieval() throws IOException {
        MatcherAssert.assertThat(invokeHttpGet(""), containsString("<app-root></app-root>"));
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
