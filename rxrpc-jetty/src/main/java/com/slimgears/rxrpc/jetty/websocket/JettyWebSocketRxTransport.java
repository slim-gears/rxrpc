package com.slimgears.rxrpc.jetty.websocket;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import com.slimgears.rxrpc.jetty.common.HttpClients;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subscribers.DisposableSubscriber;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.client.WebSocketUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
        System.out.println("Web socket connected");
        disposable.getAndSet(subscribeOutgoing(session));
        completeIfNotTerminated(connected);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        System.out.println("Web socket error: " + cause);
        errorIfNotTerminated(connected, cause);
        errorIfNotTerminated(incoming, cause);
    }

    @Override
    public void close() {
        System.out.println("Closing web socket");
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

    public static class Builder<B extends Builder<B>> {
        protected final AtomicReference<Consumer<WebSocketPolicy>> policyConfigurator = new AtomicReference<>(p -> {});

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B)this;
        }

        public B idleTimeout(Duration idleTimeout) {
            return addPolicyConfig(p -> p.setIdleTimeout(idleTimeout.toMillis()));
        }

        public B inputBufferSize(int bytes) {
            return addPolicyConfig(p -> p.setInputBufferSize(bytes));
        }

        public B outputBufferSize(int bytes) {
            return addPolicyConfig(
                    p -> p.setMaxTextMessageBufferSize(bytes),
                    p -> p.setMaxTextMessageSize(bytes));
        }

        @SafeVarargs
        private final B addPolicyConfig(Consumer<WebSocketPolicy>... config) {
            policyConfigurator.updateAndGet(pc -> Stream.concat(Stream.of(pc), Stream.of(config)).reduce(Consumer::andThen).orElse(p -> {}));
            return self();
        }
    }

    public static class ServerBuilder extends Builder<ServerBuilder> {
        public Server build() {
            return new Server(policyConfigurator.get());
        }
    }

    public static class ClientBuilder extends Builder<ClientBuilder> {
        private Supplier<SslContextFactory> sslContextFactorySupplier = SslContextFactory.Client::new;
        private Supplier<HttpClient> httpClientSupplier = () -> new HttpClient(sslContextFactorySupplier.get());
        private Consumer<ClientUpgradeRequest> requestConfigurator = request -> {};

        public ClientBuilder configureRequest(Consumer<ClientUpgradeRequest> requestConfigurator) {
            this.requestConfigurator = this.requestConfigurator.andThen(requestConfigurator);
            return this;
        }

        public ClientBuilder sslContextFactory(Supplier<SslContextFactory> contextFactorySupplier) {
            this.sslContextFactorySupplier = contextFactorySupplier;
            return this;
        }

        public ClientBuilder sslContextFactory(SslContextFactory contextFactory) {
            return sslContextFactory(() -> contextFactory);
        }

        public ClientBuilder httpClient(Supplier<HttpClient> httpClientSupplier) {
            this.httpClientSupplier = httpClientSupplier;
            return this;
        }

        public ClientBuilder httpClient(HttpClient httpClient) {
            return httpClient(() -> httpClient);
        }

        public Client build() {
            return new Client(httpClientSupplier, policyConfigurator.get(), requestConfigurator);
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

    public static ClientBuilder clientBuilder() {
        return new ClientBuilder();
    }

    public static ServerBuilder serverBuilder() {
        return new ServerBuilder();
    }

    public static class Client implements RxTransport.Client {
        private final Supplier<HttpClient> httpClientFactory;
        private final Consumer<WebSocketPolicy> policyConfigurator;
        private final Consumer<ClientUpgradeRequest> requestConfigurator;

        private Client(Supplier<HttpClient> httpClientFactory,
                       Consumer<WebSocketPolicy> policyConfigurator,
                       Consumer<ClientUpgradeRequest> requestConfigurator) {
            this.policyConfigurator = policyConfigurator;
            this.httpClientFactory = httpClientFactory;
            this.requestConfigurator = requestConfigurator;
        }

        @Override
        public Single<RxTransport> connect(URI uri) {
            try {
                HttpClients.Provider httpClientProvider = HttpClients.fromSupplier(httpClientFactory);
                HttpClient httpClient = httpClientProvider.get();
                WebSocketClient webSocketClient = new WebSocketClient(httpClient);
                policyConfigurator.accept(webSocketClient.getPolicy());
                webSocketClient.start();
                JettyWebSocketRxTransport transport = new JettyWebSocketRxTransport();
                WebSocketUpgradeRequest webSocketUpgradeRequest = new WebSocketUpgradeRequest(webSocketClient, httpClient, uri, transport);
                ClientUpgradeRequest request = new ClientUpgradeRequest(webSocketUpgradeRequest);
                request.getCookies().addAll(httpClient.getCookieStore().getCookies());
                requestConfigurator.accept(request);
                webSocketClient.connect(transport, uri, request);
                transport.incoming()
                        .doFinally(() -> {
                            webSocketClient.stop();
                            httpClientProvider.close();
                        })
                        .subscribe();
                return transport.connected.toSingle(() -> transport);
            } catch (Exception e) {
                log.error("Could not connect to: " + uri, e);
                return Single.error(e);
            }
        }
    }
}
