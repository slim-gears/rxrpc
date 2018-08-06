package com.slimgears.rxrpc.jettywebsocket;

import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.core.util.Notifier;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.function.Consumer;

public class JettyWebSocketMessageChannelServer extends WebSocketServlet implements MessageChannel.Server {
    private final Notifier<Consumer<MessageChannel>> notifier = new Notifier<>();

    @Override
    public MessageChannel.Subscription subscribe(Consumer<MessageChannel> listener) {
        return notifier.subscribe(listener)::unsubscribe;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> {
            JettyWebSocketMessageChannel channel = new JettyWebSocketMessageChannel();
            notifier.publish(c -> c.accept(channel));
            return channel;
        });
    }
}
