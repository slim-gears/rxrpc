package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jetty.http.JettyHttpRxTransportClient;
import org.eclipse.jetty.http.HttpScheme;

public class HttpServerTest extends AbstractHttpServerTest{

    @Override
    protected RxTransport.Client createClient() {
        return JettyHttpRxTransportClient.builder().build();
    }

    @Override
    protected String getTransportType() {
        return HttpScheme.HTTP.asString();
    }
}
