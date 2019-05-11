package com.slimgears.rxrpc.jettywebsocket;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subscribers.DisposableSubscriber;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JettyWebSocketRxTransport implements RxTransport, WebSocketListener {
    private final static Logger log = LoggerFactory.getLogger(JettyWebSocketRxTransport.class);
    private final AtomicReference<Disposable> disposable = new AtomicReference<>(Disposables.empty());
    private final Emitter<String> outgoing;
    private final Subject<String> outgoingSubject = BehaviorSubject.create();
    private final Subject<String> incoming = BehaviorSubject.create();
    private final CompletableSubject connected = CompletableSubject.create();

    public JettyWebSocketRxTransport() {
        this.outgoing = Emitters.fromObserver(outgoingSubject);
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
        completeIfNotTerminated(incoming);
    }

    @Override
    public synchronized void onWebSocketConnect(Session session) {
        disposable.getAndSet(subscribeOutgoing(session));
        completeIfNotTerminated(connected);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        errorIfNotTerminated(connected, cause);
        errorIfNotTerminated(incoming, cause);
    }

    @Override
    public void close() {
        outgoing().onComplete();
        incoming().onComplete();
    }

    private static boolean isTerminated(Subject<?> subject) {
        return subject.hasComplete() || subject.hasThrowable();
    }

    private static boolean isTerminated(CompletableSubject subject) {
        return subject.hasComplete() || subject.hasThrowable();
    }

    private static void errorIfNotTerminated(CompletableSubject subject, Throwable error) {
        if (!isTerminated(subject)) {
            subject.onError(error);
        }
    }

    private static void completeIfNotTerminated(CompletableSubject subject) {
        if (!isTerminated(subject)) {
            subject.onComplete();
        }
    }

    private static void errorIfNotTerminated(Subject<?> subject, Throwable error) {
        if (!isTerminated(subject)) {
            subject.onError(error);
        }
    }

    private static void completeIfNotTerminated(Subject<?> subject) {
        if (!isTerminated(subject)) {
            subject.onComplete();
        }
    }

    private Disposable subscribeOutgoing(Session session) {
        DisposableSubscriber<String> subscriber = new DisposableSubscriber<String>() {
            @Override
            public void onNext(String s) {
                session.getRemote().sendString(s, new WriteCallback() {
                    @Override
                    public void writeFailed(Throwable x) {
                        onError(x);
                    }

                    @Override
                    public void writeSuccess() {
                        request(1);
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                session.close(StatusCode.ABNORMAL, t.getMessage());
            }

            @Override
            public void onComplete() {
                session.close();
            }

            @Override
            public void onStart() {
                request(1);
            }
        };

        outgoingSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(Schedulers.io())
                .subscribe(subscriber);

        return subscriber;
    }

    public static class Builder {
        private AtomicReference<Consumer<WebSocketPolicy>> policyConfigurator = new AtomicReference<>(p -> {});

        public Builder idleTimeout(Duration idleTimeout) {
            return addPolicyConfig(p -> p.setIdleTimeout(idleTimeout.toMillis()));
        }

        public Builder inputBufferSize(int bytes) {
            return addPolicyConfig(p -> p.setInputBufferSize(bytes));
        }

        public Builder outputBufferSize(int bytes) {
            return addPolicyConfig(
                    p -> p.setMaxTextMessageBufferSize(bytes),
                    p -> p.setMaxTextMessageSize(bytes));
        }

        public Server buildServer() {
            return new Server(policyConfigurator.get());
        }

        public Client buildClient() {
            return new Client(policyConfigurator.get());
        }

        @SafeVarargs
        private final Builder addPolicyConfig(Consumer<WebSocketPolicy>... config) {
            policyConfigurator.updateAndGet(pc -> Stream.concat(Stream.of(pc), Stream.of(config)).reduce(Consumer::andThen).orElse(p -> {}));
            return this;
        }
    }

    public static class Server extends WebSocketServlet implements RxTransport.Server {
        private final Subject<RxTransport> connections = BehaviorSubject.create();
        private final Consumer<WebSocketPolicy> policyConfigurator;

        private Server(Consumer<WebSocketPolicy> policyConfigurator) {
            this.policyConfigurator = policyConfigurator;
        }

        @Override
        public void configure(WebSocketServletFactory factory) {
            policyConfigurator.accept(factory.getPolicy());
            factory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> {
                JettyWebSocketRxTransport transport = new JettyWebSocketRxTransport();
                connections.onNext(transport);
                return transport;
            });
        }

        @Override
        public Observable<RxTransport> connections() {
            return connections;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Client implements RxTransport.Client {
        private final SslContextFactory sslContextFactory = new SslContextFactory(true);
        private final WebSocketClient webSocketClient = new WebSocketClient(sslContextFactory);
        private final Consumer<WebSocketPolicy> policyConfigurator;

        private Client(Consumer<WebSocketPolicy> policyConfigurator) {
            this.policyConfigurator = policyConfigurator;
        }

        @Override
        public Single<RxTransport> connect(URI uri) {
            try {
                policyConfigurator.accept(webSocketClient.getPolicy());
                webSocketClient.start();
                JettyWebSocketRxTransport transport = new JettyWebSocketRxTransport();
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
