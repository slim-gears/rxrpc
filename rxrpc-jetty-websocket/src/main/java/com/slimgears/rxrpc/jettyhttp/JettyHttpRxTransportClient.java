package com.slimgears.rxrpc.jettyhttp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
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

public class JettyHttpRxTransportClient implements RxTransport {
    private final static Logger log = LoggerFactory.getLogger(JettyHttpRxTransportClient.class);
    private final Subject<String> incoming = BehaviorSubject.create();
    private final Subject<String> outgoingSubject = BehaviorSubject.create();
    private final Emitter<String> outgoingEmitter = Emitters.fromObserver(outgoingSubject);
    private final Disposable outgoingSubscription;
    private final Disposable pollingSubscription;
    private final HttpClient httpClient;
    private final URI uri;
    private final String clientId;

    public JettyHttpRxTransportClient(
            HttpClient httpClient,
            URI uri,
            String clientId,
            int pollingRetryCount,
            Duration pollingRetryInitialDelay,
            Duration pollingPeriod) {
        this.httpClient = httpClient;
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
        httpClient.POST(URI.create(uri + "/message"))
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
    }

    private Completable poll() {
        return Completable.create(emitter -> httpClient.POST(URI.create(uri + "/polling"))
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

        public Client build(SslContextFactory contextFactory) {
            return new Client(contextFactory, pollingRetryCount, pollingRetryInitialDelay, pollingPeriod);
        }

        public Client build() {
            return build(new SslContextFactory.Client());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Client implements RxTransport.Client {
        private final HttpClient httpClient;
        private final int pollingRetryCount;
        private final Duration pollingRetryInitialDelay;
        private final Duration pollingPeriod;

        public Client(SslContextFactory contextFactory,
                      int pollingRetryCount,
                      Duration pollingRetryInitialDelay,
                      Duration pollingPeriod) {
            this.pollingRetryCount = pollingRetryCount;
            this.pollingRetryInitialDelay = pollingRetryInitialDelay;
            this.pollingPeriod = pollingPeriod;
            this.httpClient = new HttpClient(contextFactory);
        }

        @Override
        public Single<RxTransport> connect(URI uri) {
            return Single.create(emitter -> {
                try {
                    httpClient.start();
                    httpClient.POST(URI.create(uri + "/connect"))
                            .onResponseHeader((response, field) -> {
                                if (field.getName().equals(JettyHttpAttributes.ClientIdAttribute)) {
                                    String clientId = field.getValue();
                                    emitter.onSuccess(new JettyHttpRxTransportClient(
                                            httpClient,
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
                }
            });
        }
    }
}
