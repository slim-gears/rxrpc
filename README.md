## RxRpc: End-to-end asynchronus ReactiveX-based RPC framework for Java and TypeScript

[![Build Status](https://travis-ci.org/slim-gears/rxrpc.svg?branch=dev)](https://travis-ci.org/slim-gears/rxrpc)
[![](https://jitpack.io/v/slim-gears/rxrpc.svg)](https://jitpack.io/#slim-gears/rxrpc)

# WORK IN PROGRESS...

## Goal
To provide easy-to-use asynchronous RPC framework, oriented for Java backend and Java/TypeScript client 

##### Features:

- Fully asynchronous (both client and server side)
- Automated client code generation for Java and TypeScript
- Custom dependency injection framework support
- WebSockets support


## Getting started

### Server side

##### Endpoint definition:
Endpoint is defined by class, annotated with `@RxRpcEndpoint` annotation. 
Each *RPC* method is annotated with `@RxRpcMethod` 

##### Return types:
Following return types are allowed:

- Asynchronous types
  - `Future<T>`
  - `Observable<T>`
  - `Single<T>`
  - `Maybe<T>`
  - `Completable`
- Any other type will be handled as synchronous (from server side) invocation 
 
```java
@RxRpcEndpoint("sampleEndpoint")
public class SampleEndpoint {
    @RxRpcMethod
    public Future<String> futureStringMethod(String msg, SampleRequest request) {
        return ImmediateFuture.of(
                "Server received from client: " + msg + " (id: " + request.id + ", name: " + request.name + ")");
    }

    @RxRpcMethod
    public int blockingMethod(SampleRequest request) {
        return request.id + 1;
    }

    @RxRpcMethod
    public Observable<SampleNotification> observableMethod(SampleRequest request) {
        return Observable
                .interval(0, 100, TimeUnit.MILLISECONDS)
                .take(request.id)
                .map(i -> new SampleNotification(request.name + " " + i, i));
    }
}
```

*POJO* classes can be used either as arguments or return values  

```java
public class SampleRequest {
    @JsonProperty public final int id;
    @JsonProperty public final String name;

    @JsonCreator
    public SampleRequest(@JsonProperty("id") int id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }
}
```

```java
public class SampleNotification {
    @JsonProperty public final String data;
    @JsonProperty public final long sequenceNum;

    @JsonCreator
    public SampleNotification(@JsonProperty("data") String data, @JsonProperty("sequenceNum") long sequenceNum) {
        this.data = data;
        this.sequenceNum = sequenceNum;
    }
}
```

Jetty-based embedded server example:

```java
public class SampleServer {
    private final Server jetty;
    private final JettyWebSocketRxTransport.Server transportServer = JettyWebSocketRxTransport.builder().buildServer();
    private final RxServer rxServer;

    public SampleServer(int port) {
        this.jetty = createJetty(port);
        this.rxServer = RxServer.configBuilder()
                .server(transportServer) // Use jetty WebSocket-servlet based transport
                .discoverModules() // Discover auto-generated endpoint modules
                .resolver(EndpointResolvers
                        .builder()
                        .bind(SampleEndpoint.class).to(SampleEndpointImpl.class)
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

    public void join() throws InterruptedException {
        this.jetty.join();
    }

    private Server createJetty(int port) {
        Server jetty = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder servletHolder = new ServletHolder(transportServer);
        context.addServlet(servletHolder, "/api/");
        jetty.setHandler(context);
        return jetty;
    }
}
```

### Client side

#### Java client

```java
RxClient rxClient = RxClient.forClient(JettyWebSocketRxTransport
        .builder()
        .buildClient());

SampleEndpoint_RxClient sampleEndpointClient = rxClient
        .connect(uri)
        .resolve(SampleEndpoint_RxClient.class);

sampleEndpointClient
        .observableMethod(new SampleRequest(5, "Test"))
        .map(n -> n.data)
        .test()
        .awaitDone(1000, TimeUnit.MILLISECONDS)
        .assertComplete()
        .assertValueCount(5);
```

#### Component diagram

![Diagram](http://www.plantuml.com/plantuml/png/3OpB3G8n30NlgG8A8BvpsbP2Okr6SXpB7vRoyUdfQKPpc3VrM5IUapl5m51wNEvYRm1jTpsdN_49G-sR7TbhtmkCvdgTsDH0VoeT1duXCv1GDgTcLVAN0tda_W40)