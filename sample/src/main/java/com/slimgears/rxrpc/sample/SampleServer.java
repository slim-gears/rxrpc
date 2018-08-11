package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketTransport;
import com.slimgears.rxrpc.server.EndpointDispatchers;
import com.slimgears.rxrpc.server.EndpointResolvers;
import com.slimgears.rxrpc.server.RxServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class SampleServer {
    private final Server jetty;
    private final JettyWebSocketTransport.Server msgChannelServer = new JettyWebSocketTransport.Server();
    private final RxServer rxServer;

    public SampleServer(int port) {
        this.jetty = createJetty(port);
        this.rxServer = RxServer.forConfig(RxServer.Config
                .builder()
                .server(msgChannelServer) // Use jetty WebSocket-servlet based transport
                .resolver(EndpointResolvers.defaultConstructorResolver()) // No dependency injection
                .modules(EndpointDispatchers.discover()) // Discover auto-generated endpoint modules
                .objectMapper(new ObjectMapper()) // You may provide preconfigured ObjectMapper
                .build());
    }

    public void start() throws Exception {
        this.jetty.start();
        this.rxServer.start();
    }

    public void stop() throws Exception {
        this.rxServer.stop();
        this.jetty.stop();
    }

    public void join() throws InterruptedException {
        this.jetty.join();
    }

    private Server createJetty(int port) {
        Server jetty = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder servletHolder = new ServletHolder(msgChannelServer);
        context.addServlet(servletHolder, "/api/");
        jetty.setHandler(context);
        return jetty;
    }
}
