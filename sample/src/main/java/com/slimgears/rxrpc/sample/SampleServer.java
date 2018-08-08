package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketTransport;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.function.Consumer;

public class SampleServer implements Transport.Server {
    private final Server jetty;
    private final JettyWebSocketTransport.Server msgChannelServer = new JettyWebSocketTransport.Server();

    public SampleServer(int port) {
        this.jetty = new Server(port);
        this.jetty.setHandler(createServletContextHandler());
    }

    public void start() {
        try {
            this.jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            this.jetty.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void join() {
        try {
            this.jetty.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ServletContextHandler createServletContextHandler() {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder servletHolder = new ServletHolder(msgChannelServer);
        context.addServlet(servletHolder, "/socket/");
        return context;
    }

    @Override
    public Transport.Subscription subscribe(Consumer<Transport> listener) {
        return msgChannelServer.subscribe(listener);
    }
}
