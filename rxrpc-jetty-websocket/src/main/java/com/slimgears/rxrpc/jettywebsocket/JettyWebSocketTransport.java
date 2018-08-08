package com.slimgears.rxrpc.jettywebsocket;

import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.core.util.ErrorFuture;
import com.slimgears.rxrpc.core.util.MappedFuture;
import com.slimgears.rxrpc.core.util.Notifier;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class JettyWebSocketTransport implements Transport, WebSocketListener {
    private final static Logger log = LoggerFactory.getLogger(JettyWebSocketTransport.class);
    private final AtomicReference<org.eclipse.jetty.websocket.api.Session> webSocketSession = new AtomicReference<>();
    private final AtomicReference<Session> channelSession = new AtomicReference<>();
    private final Notifier<Listener> notifier = new Notifier<>();

    class InternalSession implements Transport.Session {
        private final org.eclipse.jetty.websocket.api.Session session;

        InternalSession(org.eclipse.jetty.websocket.api.Session session) {
            this.session = session;
        }

        @Override
        public void send(String message) {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                onWebSocketError(e);
            }
        }

        @Override
        public void close() {
            session.close();
            webSocketSession.set(null);
        }
    }

    @Override
    public synchronized Subscription subscribe(Listener listener) {
        Optional.ofNullable(channelSession.get()).ifPresent(listener::onConnected);
        return notifier.subscribe(listener)::unsubscribe;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {

    }

    @Override
    public void onWebSocketText(String message) {
        log.debug("Message: {}", message);
        notifier.publish(l -> l.onMessage(message));
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        notifier.publish(Listener::onClosed);
        webSocketSession.set(null);
    }

    @Override
    public synchronized void onWebSocketConnect(org.eclipse.jetty.websocket.api.Session session) {
        this.webSocketSession.set(session);
        Transport.Session msgChannelSession = new InternalSession(session);
        channelSession.set(msgChannelSession);
        notifier.publish(l -> l.onConnected(msgChannelSession));
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        notifier.publish(l -> l.onError(cause));
    }

    public static class Server extends WebSocketServlet implements Transport.Server {
        private final Notifier<Consumer<Transport>> notifier = new Notifier<>();

        @Override
        public Subscription subscribe(Consumer<Transport> listener) {
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
                JettyWebSocketTransport channel = new JettyWebSocketTransport();
                notifier.publish(c -> c.accept(channel));
                return channel;
            });
        }
    }

    public static class Client implements Transport.Client {
        private final WebSocketClient webSocketClient = new WebSocketClient();

        public Client() {
            try {
                webSocketClient.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Future<Transport> connect(URI uri) {
            try {
                JettyWebSocketTransport channel = new JettyWebSocketTransport();
                return MappedFuture.of(webSocketClient.connect(channel, uri), s -> channel);
            } catch (IOException e) {
                return ErrorFuture.of(e);
            }
        }
    }
}
