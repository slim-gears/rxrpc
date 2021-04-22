package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jettyhttp.JettyHttpRxTransportClient;
import org.eclipse.jetty.http.HttpScheme;

public class HttpServerTest extends AbstractHttpServerTest{

    @Override
    protected RxTransport.Client createClient() {
        return JettyHttpRxTransportClient.builder().buildClient();
    }

    @Override
    protected String getTransportType() {
        return HttpScheme.HTTP.asString();
    }
}
