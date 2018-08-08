package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketTransport;

import java.net.URI;
import java.util.concurrent.Future;

public class SampleClient implements Transport.Client {
    private final JettyWebSocketTransport.Client client = new JettyWebSocketTransport.Client();

    @Override
    public Future<Transport> connect(URI uri) {
        return client.connect(uri);
    }
}
