package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jettyhttp.JettyHttpRxTransportServer;
import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.RxServer;
import com.slimgears.util.generic.ServiceResolvers;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleServer<T extends RxTransport.Server & Servlet> {
    private final Server jetty;
    private final RxServer rxServer;
    private final String transportType;
    private final T transportServer;

    public static SampleServer<JettyWebSocketRxTransport.Server> forWebSocket(int port) {
        return forTransport(port, JettyWebSocketRxTransport.builder().buildServer(), "ws");
    }

    public static SampleServer<JettyHttpRxTransportServer.Server> forHttp(int port) {
        return forTransport(port, JettyHttpRxTransportServer.builder().buildServer(), "http");
    }

    public static <T extends RxTransport.Server & Servlet> SampleServer<T> forTransport(int port, T transport, String transportType) {
        return new SampleServer<>(port, transport, transportType);
    }

    private SampleServer(int port, T transportServer, String transportType) {
        this.transportServer = transportServer;
        this.jetty = createJetty(port);
        this.rxServer = RxServer.configBuilder()
                .server(transportServer) // Use jetty [WebSocket | Http]-servlet based transport
                .modules(
                        EndpointRouters.moduleByName("sampleModule"),
                        EndpointRouters.discover())
                .resolver(ServiceResolvers
                        .builder()
                        .bind(SampleEndpoint.class).to(SampleEndpointImpl.class)
                        .bind(SayHelloEndpoint.class).to(SayHelloEndpointImpl.class)
                        .bind(RepetitionSayHelloEndpoint.class).to(RepetitionSayHelloEndpointImpl.class)
                        .build())
                .createServer();
        this.transportType = transportType;
    }

    public void start() throws Exception {
        this.jetty.start();
        this.rxServer.start();
    }

    public void stop() throws Exception {
        this.rxServer.stop();
        this.jetty.stop();
    }

    private Server createJetty(int port) {
        Server jetty = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(transportServer), "/api/*");
        context.addServlet(new ServletHolder(new DefaultServlet()), "/*");
        URL webResourceUrl = getClass().getResource("/web");
        String resourceBasePath = webResourceUrl.toExternalForm();
        context.setResourceBase(resourceBasePath);
        context.setWelcomeFiles(new String[] { "index.html"});

        ErrorPageErrorHandler errorPageErrorHandler = new ErrorPageErrorHandler();
        errorPageErrorHandler.addErrorPage(HttpStatus.NOT_FOUND_404, "/");
        context.setErrorHandler(errorPageErrorHandler);

        HandlerWrapper responseWrapper = new HandlerWrapper() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                response.addCookie(new Cookie("RxRpcTransport", transportType));
                super.handle(target, baseRequest, request, response);
            }
        };

        responseWrapper.setHandler(context);

        jetty.setHandler(responseWrapper);
        return jetty;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String... args) {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.WARNING);
        int port = 8000;
        SampleServer<?> httpServer = SampleServer.forHttp(port);
        SampleServer<?> wsServer = SampleServer.forWebSocket(port + 1);
        try {
            httpServer.start();
            wsServer.start();
            System.out.printf("Server started and listening at: http://localhost:%d\nPress <Enter> to stop.%n", port);
            System.in.read();
            httpServer.stop();
            wsServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
