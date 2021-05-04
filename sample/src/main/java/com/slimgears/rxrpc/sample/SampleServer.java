package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.jetty.http.JettyHttpRxTransportServer;
import com.slimgears.rxrpc.jetty.websocket.JettyWebSocketRxTransport;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.RxServer;
import com.slimgears.util.generic.ServiceResolvers;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleServer<T extends RxTransport.Server & Servlet> {
    private final Server jetty;
    private final RxServer rxServer;
    private final String transportType;
    private final T transportServer;

    public static SampleServer<JettyWebSocketRxTransport.Server> forWebSocket(int port) {
        return forTransport(port, JettyWebSocketRxTransport.serverBuilder().build(), HttpScheme.WS.asString());
    }

    public static SampleServer<JettyHttpRxTransportServer.Server> forHttp(int port) {
        return forTransport(port, JettyHttpRxTransportServer.builder().build(), HttpScheme.HTTP.asString());
    }

    public static SampleServer<JettyHttpRxTransportServer.Server> forHttps(int port) {
        return forTransport(port, JettyHttpRxTransportServer.builder().build(), HttpScheme.HTTPS.asString());
    }

    public static <T extends RxTransport.Server & Servlet> SampleServer<T> forTransport(int port, T transport, String transportType) {
        return new SampleServer<>(port, transport, transportType);
    }

    private SampleServer(int port, T transportServer, String transportType) {
        this.transportServer = transportServer;
        this.transportType = transportType;
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
        Server jetty = new Server();

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

        // HTTP Configuration
        HttpConfiguration http = new HttpConfiguration();
        http.addCustomizer(new SecureRequestCustomizer());

        ServerConnector connector;

        if (transportType.equals(HttpScheme.HTTPS.asString())) {
            final String keystoreResource = "keystore.jks";
            final String keystorePassword = "123456";
            final String keyManagerPassword = "123456";

            // HTTPS configuration
            HttpConfiguration https = new HttpConfiguration(http);
            https.addCustomizer(new SecureRequestCustomizer());

            // Configuring SSL
            SslContextFactory sslContextFactory = new SslContextFactory.Server.Server();

            // Defining keystore path and passwords
            String keyStorePath = Optional.ofNullable(getClass().getClassLoader().getResource(keystoreResource))
                    .map(URL::toExternalForm)
                    .orElseThrow(() -> new RuntimeException("Unable to find " + keystoreResource));
            sslContextFactory.setKeyStorePath(keyStorePath);
            sslContextFactory.setKeyStorePassword(keystorePassword);
            sslContextFactory.setKeyManagerPassword(keyManagerPassword);

            // Configuring the connector
            connector = new ServerConnector(jetty,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.toString()),
                    new HttpConnectionFactory(https));
        } else {
            connector = new ServerConnector(jetty);
            connector.addConnectionFactory(new HttpConnectionFactory(http));
        }

        // Setting port
        connector.setPort(port);

        // Setting HTTP and HTTPS connectors
        jetty.setConnectors(new Connector[] { connector });

        return jetty;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String... args) {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.WARNING);
        int httpPort = 8000, wsPort = 8001, httpsPort = 8002;
        SampleServer<?> httpServer = SampleServer.forHttp(httpPort);
        SampleServer<?> wsServer = SampleServer.forWebSocket(wsPort);
        SampleServer<?> httpsServer = SampleServer.forHttps(httpsPort);
        try {
            httpServer.start();
            httpsServer.start();
            wsServer.start();
            System.out.printf("Server started and listening at:\n" +
                            "http://localhost:%d\n" +
                            "https://localhost:%d\n" +
                            "http://localhost:%d (WebSocket)\n" +
                            "Press <Enter> to stop.%n",
                    httpPort, httpsPort, wsPort);
            System.in.read();
            httpServer.stop();
            httpsServer.stop();
            wsServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
