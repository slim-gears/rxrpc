package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.jettywebsocket.JettyWebSocketRxTransport;
import com.slimgears.rxrpc.server.EndpointRouters;
import com.slimgears.rxrpc.server.RxServer;
import com.slimgears.util.generic.ServiceResolvers;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleServer {
    private final Server jetty;
    private final JettyWebSocketRxTransport.Server transportServer = JettyWebSocketRxTransport.builder().buildServer();
    private final RxServer rxServer;

    public SampleServer(int port) {
        this.jetty = createJetty(port);
        this.rxServer = RxServer.configBuilder()
                .server(transportServer) // Use jetty WebSocket-servlet based transport
                .modules(
                        EndpointRouters.moduleByName("sampleModule"),
                        new SayHelloEndpoint_RxModule(),
                        new SampleMetaEndpointImplInteger_RxModule(),
                        new SampleMetaEndpointImplSampleRequest_RxModule())
                .resolver(ServiceResolvers
                        .builder()
                        .bind(SampleEndpoint.class).to(SampleEndpointImpl.class)
                        .bind(SayHelloEndpoint.class).to(SayHelloEndpointImpl.class)
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
        Server jetty = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(transportServer), "/api/");
        context.addServlet(new ServletHolder(new DefaultServlet()), "/*");
        URL webResourceUrl = getClass().getResource("/web");
        String resourceBasePath = webResourceUrl.toExternalForm();
        context.setResourceBase(resourceBasePath);
        context.setWelcomeFiles(new String[] { "index.html"});

        ErrorPageErrorHandler errorPageErrorHandler = new ErrorPageErrorHandler();
        errorPageErrorHandler.addErrorPage(HttpStatus.NOT_FOUND_404, "/");
        context.setErrorHandler(errorPageErrorHandler);

        jetty.setHandler(context);
        return jetty;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String... args) {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.WARNING);
        int port = 8000;
        SampleServer server = new SampleServer(port);
        try {
            server.start();
            System.out.println(String.format("Server started and listening at: http://localhost:%d\nPress <Enter> to stop.", port));
            System.in.read();
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
