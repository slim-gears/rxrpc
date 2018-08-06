package com.slimgears.rxrpc.jettywebsocket;

import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.core.util.Notifier;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class JettyWebSocketMessageChannel implements MessageChannel, WebSocketListener {
    private final AtomicReference<org.eclipse.jetty.websocket.api.Session> webSocketSession = new AtomicReference<>();
    private final AtomicReference<Session> channelSession = new AtomicReference<>();
    private final Notifier<Listener> notifier = new Notifier<>();

    class InternalSession implements MessageChannel.Session {
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
        MessageChannel.Session msgChannelSession = new InternalSession(session);
        channelSession.set(msgChannelSession);
        notifier.publish(l -> l.onConnected(msgChannelSession));
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        notifier.publish(l -> l.onError(cause));
    }
}
