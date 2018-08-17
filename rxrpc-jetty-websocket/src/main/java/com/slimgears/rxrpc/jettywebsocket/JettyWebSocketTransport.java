package com.slimgears.rxrpc.jettywebsocket;

import com.slimgears.rxrpc.core.Transport;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class JettyWebSocketTransport implements Transport, WebSocketListener {
    private final static Logger log = LoggerFactory.getLogger(JettyWebSocketTransport.class);
    private final AtomicReference<Disposable> disposable = new AtomicReference<>(Disposables.empty());
    private final Emitter<String> outgoing;
    private final Subject<String> outgoingSubject = BehaviorSubject.create();
    private final Subject<String> incoming = BehaviorSubject.create();
    private final CompletableSubject connected = CompletableSubject.create();

    public JettyWebSocketTransport() {
        this.outgoing = new Emitter<String>() {
            @Override
            public void onNext(String value) {
                outgoingSubject.onNext(value);
            }

            @Override
            public void onError(Throwable error) {
                outgoingSubject.onError(error);
            }

            @Override
            public void onComplete() {
                outgoingSubject.onComplete();
            }
        };
    }

    @Override
    public Emitter<String> outgoing() {
        return outgoing;
    }

    @Override
    public Subject<String> incoming() {
        return incoming;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {

    }

    @Override
    public void onWebSocketText(String message) {
        incoming.onNext(message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        if (statusCode == StatusCode.NORMAL) {
            incoming.onComplete();
        } else {
            incoming.onError(new RuntimeException("Connection closed with status: " + statusCode + " (" + reason + ")"));
        }
    }

    @Override
    public synchronized void onWebSocketConnect(Session session) {
        disposable.getAndSet(outgoingSubject.subscribe(
                msg -> session.getRemote().sendString(msg),
                error -> session.close(StatusCode.ABNORMAL, error.getMessage()),
                session::close));
        connected.onComplete();
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        incoming.onError(cause);
    }

    @Override
    public void close() {
        outgoing().onComplete();
        incoming().onComplete();
    }

    public static class Server extends WebSocketServlet implements Transport.Server {
        private final Subject<Transport> connections = BehaviorSubject.create();

        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> {
                JettyWebSocketTransport transport = new JettyWebSocketTransport();
                connections.onNext(transport);
                return transport;
            });
        }

        @Override
        public Observable<Transport> connections() {
            return connections;
        }
    }

    public static class Client implements Transport.Client {
        private final WebSocketClient webSocketClient = new WebSocketClient();

        @Override
        public Single<Transport> connect(URI uri) {
            try {
                webSocketClient.start();
                JettyWebSocketTransport transport = new JettyWebSocketTransport();
                webSocketClient.connect(transport, uri);
                transport.incoming().subscribe(Functions.emptyConsumer(), e -> webSocketClient.stop(), webSocketClient::stop);
                return transport.connected.toSingle(() -> transport);
            } catch (Exception e) {
                log.error("Could not connect to: " + uri, e);
                return Single.error(e);
            }
        }
    }
}
