package com.slimgears.rxrpc.sample;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slimgears.rxrpc.client.EndpointFactories;
import com.slimgears.rxrpc.client.RxClient;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.EndpointResolvers;
import com.slimgears.rxrpc.server.RxServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class SampleServerTest {
    private final static int port = 8000;
    private final static URI uri = URI.create("ws://localhost:" + port + "/socket/");

    @BeforeClass
    public static void init() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    @Test
    public void testClientServer() throws ExecutionException, InterruptedException {
        SampleServer server = new SampleServer(port);
        SampleClient client = new SampleClient();

        RxServer rxServer = RxServer.forConfig(RxServer.Config
                .builder()
                .server(server)
                .resolver(EndpointResolvers.defaultConstructorResolver())
                .modules(EndpointDispatchers.discover())
                .objectMapper(new ObjectMapper())
                .build());

        rxServer.start();

        RxClient rxClient = RxClient.forConfig(RxClient.Config
                .builder()
                .objectMapper(new ObjectMapper())
                .endpointFactory(EndpointFactories.defaultConstructorFactory())
                .client(client)
                .build());

        SampleEndpoint_RxClient sampleEndpointClient = rxClient.connect(uri).resolve(SampleEndpoint_RxClient.class);
        String msgFromServer = sampleEndpointClient.futureStringMethod("Test", new SampleRequest(3, "sampleName")).get();
        Assert.assertEquals("Server received from client: Test (id: 3, name: sampleName)", msgFromServer);

        int intFromServer = sampleEndpointClient.intMethod(new SampleRequest(4, "sampleName"));
        Assert.assertEquals(5, intFromServer);

        rxServer.stop();
    }
}