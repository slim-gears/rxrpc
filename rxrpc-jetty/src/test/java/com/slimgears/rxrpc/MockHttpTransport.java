package com.slimgears.rxrpc;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import com.slimgears.rxrpc.jetty.common.HttpClients;
import com.slimgears.rxrpc.jetty.http.JettyHttpAttributes;
import com.slimgears.rxrpc.jetty.http.JettyHttpRxTransportClient;
import com.slimgears.rxrpc.jetty.http.JettyHttpRxTransportServer;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URI;
import java.time.Duration;
import java.util.function.Supplier;

class MockHttpTransport {
    private static final Subject<String> clientIncoming = PublishSubject.create();
    private static final Subject<String> serverIncoming = PublishSubject.create();
    private static final serverTransport.Server server = createServer();
    private final clientTransport.Client client = createClient();

    protected clientTransport.Client getClientTransport() {
        return client;
    }

    protected serverTransport.Server getServerTransport() {
        return server;
    }

    protected clientTransport.Client createClient() {
        return clientTransport.builder()
                .incoming(clientIncoming)
                .outgoing(serverIncoming)
                .build();
    }

    protected static serverTransport.Server createServer() {
        return serverTransport.builder()
                .incoming(serverIncoming)
                .outgoing(clientIncoming)
                .build();
    }

    static class clientTransport extends JettyHttpRxTransportClient {

        Subject<String> incoming;
        Observer<String> outgoing;

        public clientTransport(HttpClients.Provider httpClientProvider, URI uri, String clientId, int pollingRetryCount,
                               Duration pollingRetryInitialDelay, Duration pollingPeriod,
                               Subject<String> incoming, Observer<String> outgoing) {
            super(httpClientProvider, uri, clientId, pollingRetryCount, pollingRetryInitialDelay, pollingPeriod);
            this.incoming = incoming;
            this.outgoing = outgoing;
        }

        @Override
        public Emitter<String> outgoing() {
            return Emitters.fromObserver(outgoing);
        }

        @Override
        public Subject<String> incoming() {
            return incoming;
        }

        public static class Client extends JettyHttpRxTransportClient.Client {
            private final int pollingRetryCount;
            private final Duration pollingRetryInitialDelay;
            private final Duration pollingPeriod;
            Supplier<HttpClient> httpClientSupplier;
            Subject<String> incoming;
            Observer<String> outgoing;

            public Client(Supplier<HttpClient> httpClientSupplier, int pollingRetryCount, Duration pollingRetryInitialDelay,
                          Duration pollingPeriod, Subject<String> incoming, Observer<String> outgoing) {
                super(httpClientSupplier, pollingRetryCount, pollingRetryInitialDelay, pollingPeriod);
                this.httpClientSupplier = httpClientSupplier;
                this.pollingPeriod = pollingPeriod;
                this.pollingRetryInitialDelay = pollingRetryInitialDelay;
                this.pollingRetryCount = pollingRetryCount;
                this.incoming = incoming;
                this.outgoing = outgoing;
            }

            @Override
            public Single<RxTransport> connect(URI uri) {
                server.connect();
                return Single.just(new clientTransport(HttpClients.fromSupplier(httpClientSupplier),
                        uri, "123", pollingRetryCount, pollingRetryInitialDelay, pollingPeriod, incoming, outgoing));
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends JettyHttpRxTransportClient.Builder {
            Subject<String> incoming;
            Observer<String> outgoing;

            public Builder incoming(Subject<String> incoming) {
                this.incoming = incoming;
                return this;
            }

            public Builder outgoing(Observer<String> outgoing) {
                this.outgoing = outgoing;
                return this;
            }

            @Override
            public Client build() {
                return new Client(() -> new HttpClient(new SslContextFactory.Client(true)),
                        JettyHttpAttributes.ClientPollingRetryCount,
                        JettyHttpAttributes.ClientPollingRetryInitialDelay,
                        JettyHttpAttributes.ClientPollingPeriod,
                        incoming,
                        outgoing);
            }
        }
    }


    static class serverTransport extends JettyHttpRxTransportServer {

        Subject<String> incoming;
        Observer<String> outgoing;

        public serverTransport(Duration keepAliveTimeout, String clientId, Subject<String> incoming, Observer<String> outgoing) {
            super(keepAliveTimeout, clientId);
            this.incoming = incoming;
            this.outgoing = outgoing;
        }

        @Override
        public Emitter<String> outgoing() {
            return Emitters.fromObserver(outgoing);
        }

        @Override
        public Subject<String> incoming() {
            return incoming;
        }

        public static class Server extends JettyHttpRxTransportServer.Server {
            public final Subject<RxTransport> connections = BehaviorSubject.create();
            private final Duration keepAliveTimeout;
            Subject<String> incoming;
            Observer<String> outgoing;

            public Server(Duration keepAliveTimeout, Subject<String> incoming, Observer<String> outgoing) {
                super(keepAliveTimeout);
                this.incoming = incoming;
                this.outgoing = outgoing;
                this.keepAliveTimeout = keepAliveTimeout;
            }

            public void connect() {
                connections.onNext(new serverTransport(keepAliveTimeout, "123", incoming, outgoing));
            }

            @Override
            public Observable<RxTransport> connections() {
                return connections;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends JettyHttpRxTransportServer.Builder {
            Subject<String> incoming;
            Observer<String> outgoing;

            public Builder incoming(Subject<String> incoming) {
                this.incoming = incoming;
                return this;
            }

            public Builder outgoing(Observer<String> outgoing) {
                this.outgoing = outgoing;
                return this;
            }

            @Override
            public Server build() {
                return new Server(JettyHttpAttributes.ServerKeepAliveTimeout, incoming, outgoing);
            }
        }
    }
}
