package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import org.eclipse.jetty.http.HttpScheme;

public class WebSocketServerTest extends AbstractServerTest<JettyWebSocketRxTransport.Server> {

    @Override
    protected RxTransport.Client createClient() {
        return JettyWebSocketRxTransport.builder().buildClient();
    }

    @Override
    protected JettyWebSocketRxTransport.Server createServer() {
        return JettyWebSocketRxTransport.builder().buildServer();
    }

    @Override
    protected String getTransportType() {
        return HttpScheme.WS.asString();
    }
}
