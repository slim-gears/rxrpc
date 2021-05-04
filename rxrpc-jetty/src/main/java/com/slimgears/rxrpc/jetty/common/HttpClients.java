package com.slimgears.rxrpc.jetty.common;

import com.slimgears.util.stream.Lazy;
import com.slimgears.util.stream.Safe;
import org.eclipse.jetty.client.HttpClient;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HttpClients {
    public interface Provider extends Supplier<HttpClient>, AutoCloseable {
    }

    public static Provider fromSupplier(Supplier<HttpClient> httpClientSupplier) {
        AtomicReference<AutoCloseable> cleanUp = new AtomicReference<>(() -> {});
        Lazy<HttpClient> lazy = Lazy.fromCallable(() -> {
            HttpClient httpClient = httpClientSupplier.get();
            if (!httpClient.isStarted()) {
                httpClient.start();
                cleanUp.set(httpClient::stop);
            }
            return httpClient;
        });

        return new Provider() {
            @Override
            public void close() throws Exception {
                cleanUp.get().close();
            }

            @Override
            public HttpClient get() {
                return lazy.get();
            }
        };
    }
}
