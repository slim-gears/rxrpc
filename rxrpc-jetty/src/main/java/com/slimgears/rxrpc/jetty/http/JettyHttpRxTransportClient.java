package com.slimgears.rxrpc.jetty.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import com.slimgears.rxrpc.jetty.common.HttpClients;
import com.slimgears.rxrpc.jetty.websocket.JettyWebSocketRxTransport;
import com.slimgears.util.rx.Completables;
import io.reactivex.Completable;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class JettyHttpRxTransportClient implements RxTransport {
    private final static Logger log = LoggerFactory.getLogger(JettyHttpRxTransportClient.class);
    private final Subject<String> incoming = BehaviorSubject.create();
    private final Subject<String> outgoingSubject = BehaviorSubject.create();
    private final Emitter<String> outgoingEmitter = Emitters.fromObserver(outgoingSubject);
    private final Disposable outgoingSubscription;
    private final Disposable pollingSubscription;
    private final HttpClients.Provider httpClientProvider;
    private final URI uri;
    private final String clientId;

    public JettyHttpRxTransportClient(
            HttpClients.Provider httpClientProvider,
            URI uri,
            String clientId,
            int pollingRetryCount,
            Duration pollingRetryInitialDelay,
            Duration pollingPeriod) {
        this.httpClientProvider = httpClientProvider;
        this.uri = uri;
        this.clientId = clientId;
        outgoingSubscription = outgoingSubject.subscribe(
                this::sendMessage,
                incoming::onError);
        pollingSubscription = Observable.interval(pollingPeriod.toMillis(), TimeUnit.MILLISECONDS)
                .flatMapCompletable(i -> poll())
                .compose(Completables.backOffDelayRetry(e -> true,
                        pollingRetryInitialDelay,
                        pollingRetryCount))
                .subscribe(incoming::onComplete, incoming::onError);
    }

    private void sendMessage(String message) {
        httpClientProvider.get().POST(URI.create(uri + "/message"))
                .header(JettyHttpAttributes.ClientIdAttribute, clientId)
                .content(new StringContentProvider(message), "text/plain")
                .onResponseContent(this::onContent)
                .send(result -> {
                    if (result.isFailed()) {
                        outgoingEmitter.onError(result.getFailure());
                    }
                });
    }

    @Override
    public Emitter<String> outgoing() {
        return outgoingEmitter;
    }

    @Override
    public Subject<String> incoming() {
        return incoming;
    }

    @Override
    public void close() {
        outgoingSubscription.dispose();
        pollingSubscription.dispose();
        incoming().onComplete();
        try {
            httpClientProvider.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Completable poll() {
        return Completable.create(emitter -> httpClientProvider.get().POST(URI.create(uri + "/polling"))
                .header(JettyHttpAttributes.ClientIdAttribute, clientId)
                .onResponseContent(this::onContent)
                .send(result -> {
                    if (result.isSucceeded()) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(result.getFailure());
                    }
                }));
    }

    private void onContent(Response response, ByteBuffer content) {
        Arrays.stream(new Gson().fromJson(StandardCharsets.UTF_8.decode(content).toString(), JsonObject[].class))
                .forEach(o -> incoming().onNext(o.toString()));
    }

    public static class Builder {
        private int pollingRetryCount = JettyHttpAttributes.ClientPollingRetryCount;
        private Duration pollingRetryInitialDelay = JettyHttpAttributes.ClientPollingRetryInitialDelay;
        private Duration pollingPeriod = JettyHttpAttributes.ClientPollingPeriod;
        private Supplier<SslContextFactory> sslContextFactorySupplier = SslContextFactory.Client::new;
        private Supplier<HttpClient> httpClientSupplier = () -> new HttpClient(sslContextFactorySupplier.get());

        public Builder pollingRetryCount(int pollingRetryCount) {
            this.pollingRetryCount = pollingRetryCount;
            return this;
        }

        public Builder pollingRetryInitialDelay(Duration pollingRetryInitialDelay) {
            this.pollingRetryInitialDelay = pollingRetryInitialDelay;
            return this;
        }

        public Builder pollingPeriod(Duration pollingPeriod) {
            this.pollingPeriod = pollingPeriod;
            return this;
        }

        public Builder sslContextFactory(Supplier<SslContextFactory> contextFactorySupplier) {
            this.sslContextFactorySupplier = contextFactorySupplier;
            return this;
        }

        public Builder sslContextFactory(SslContextFactory contextFactory) {
            return sslContextFactory(() -> contextFactory);
        }

        public Builder httpClient(Supplier<HttpClient> httpClientSupplier) {
            this.httpClientSupplier = httpClientSupplier;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            return httpClient(() -> httpClient);
        }

        public Client build() {
            return new Client(httpClientSupplier, pollingRetryCount, pollingRetryInitialDelay, pollingPeriod);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Client implements RxTransport.Client {
        private final Supplier<HttpClient> httpClientSupplier;
        private final int pollingRetryCount;
        private final Duration pollingRetryInitialDelay;
        private final Duration pollingPeriod;

        public Client(Supplier<HttpClient> httpClientSupplier,
                      int pollingRetryCount,
                      Duration pollingRetryInitialDelay,
                      Duration pollingPeriod) {
            this.pollingRetryCount = pollingRetryCount;
            this.pollingRetryInitialDelay = pollingRetryInitialDelay;
            this.pollingPeriod = pollingPeriod;
            this.httpClientSupplier = httpClientSupplier;
        }

        @Override
        public Single<RxTransport> connect(URI uri) {
            return Single.create(emitter -> {
                HttpClients.Provider httpClientProvider = HttpClients.fromSupplier(httpClientSupplier);
                try {
                    HttpClient httpClient = httpClientProvider.get();
                    httpClient.POST(URI.create(uri + "/connect"))
                            .onResponseHeader((response, field) -> {
                                if (field.getName().equals(JettyHttpAttributes.ClientIdAttribute)) {
                                    String clientId = field.getValue();
                                    emitter.onSuccess(new JettyHttpRxTransportClient(
                                            httpClientProvider,
                                            uri,
                                            clientId,
                                            pollingRetryCount,
                                            pollingRetryInitialDelay,
                                            pollingPeriod));
                                }
                                return true;
                            })
                            .send(result -> {
                                if (result.isFailed()) {
                                    log.error("Could not connect to: " + uri, result.getFailure());
                                    emitter.onError(result.getFailure());
                                }
                            });
                } catch (Exception e) {
                    log.error("Could not start the httpClient", e);
                    emitter.onError(e);
                    httpClientProvider.close();
                }
            });
        }
    }
}
