package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.api.MessageChannel;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketMessageChannelServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;
import java.util.function.Consumer;

public class SampleServer implements MessageChannel.Server {
    private final Server jetty;
    private final JettyWebSocketMessageChannelServer msgChannelServer = new JettyWebSocketMessageChannelServer();

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
    public MessageChannel.Subscription subscribe(Consumer<MessageChannel> listener) {
        return msgChannelServer.subscribe(listener);
    }
}
