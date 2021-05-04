package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jetty.http.JettyHttpRxTransportClient;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;

public class HttpsServerTest extends AbstractHttpServerTest{

    @Override
    protected RxTransport.Client createClient() {
        return JettyHttpRxTransportClient.builder()
                .sslContextFactory(new SslContextFactory.Client(true))
                .build();
    }

    @Override
    protected String getTransportType() {
        return HttpScheme.HTTPS.asString();
    }

    @Test
    public void testStaticContentRetrieval() {}
}
