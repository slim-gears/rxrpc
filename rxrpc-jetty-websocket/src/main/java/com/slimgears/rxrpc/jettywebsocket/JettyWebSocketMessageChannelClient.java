package com.slimgears.rxrpc.jettywebsocket;

import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.core.util.ErrorFuture;
import com.slimgears.rxrpc.core.util.MappedFuture;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;

public class JettyWebSocketMessageChannelClient implements MessageChannel.Client {
    private final WebSocketClient webSocketClient = new WebSocketClient();

    public JettyWebSocketMessageChannelClient() {
        try {
            webSocketClient.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<MessageChannel> connect(URI uri) {
        try {
            JettyWebSocketMessageChannel channel = new JettyWebSocketMessageChannel();
            return MappedFuture.of(webSocketClient.connect(channel, uri), s -> channel);
        } catch (IOException e) {
            return ErrorFuture.of(e);
        }
    }
}
