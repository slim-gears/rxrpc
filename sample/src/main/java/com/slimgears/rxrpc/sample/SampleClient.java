package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketMessageChannelClient;

import java.net.URI;
import java.util.concurrent.Future;

public class SampleClient implements MessageChannel.Client {
    private final JettyWebSocketMessageChannelClient client = new JettyWebSocketMessageChannelClient();

    @Override
    public Future<MessageChannel> connect(URI uri) {
        return client.connect(uri);
    }
}
