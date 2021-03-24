package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;

public class WebSocketServerTest extends AbstractServerTest<JettyWebSocketRxTransport.Server> {
    @Override
    protected String getUriScheme() {
        return "ws://";
    }

    @Override
    protected RxTransport.Client createClient() {
        return JettyWebSocketRxTransport.builder().buildClient();
    }

    @Override
    protected JettyWebSocketRxTransport.Server createServer() {
        return JettyWebSocketRxTransport.builder().buildServer();
    }
}
