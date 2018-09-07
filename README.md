## RxRpc: End-to-end asynchronus ReactiveX-based RPC framework for Java and TypeScript

[![Build Status](https://travis-ci.org/slim-gears/rxrpc.svg?branch=master)](https://travis-ci.org/slim-gears/rxrpc)
[![](https://jitpack.io/v/slim-gears/rxrpc.svg)](https://jitpack.io/#slim-gears/rxrpc)
[![npm version](https://badge.fury.io/js/ng-rxrpc.svg)](https://badge.fury.io/js/ng-rxrpc)

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
  - `Observable<T>`
  - `Single<T>`
  - `Maybe<T>`
  - `Completable`
  - `Publisher<T>`
  - `Future<T>`
- Any other type will be handled as synchronous (from server side) invocation 

 #### Endpoint definition example
 
```java
@RxRpcEndpoint
public interface SayHelloEndpoint {
    @RxRpcMethod
    Observable<String> sayHello(String name);
}

public class SayHelloEndpointImpl implements SayHelloEndpoint {
    public Observable<String> sayHello(String name) {
        Observable.just("Hello, " + name).delay(2, TimeUnit.SECONDS);
    }
}
```

#### Jetty-based embedded server, serving the endpoint, defined above

```java
public class SampleServer {
    private final Server jetty;
    private final JettyWebSocketRxTransport.Server transportServer = JettyWebSocketRxTransport
            .builder()
            .buildServer();
            
    private final RxServer rxServer;

    public SampleServer(int port) {
        this.jetty = createJetty(port);
        this.rxServer = RxServer.configBuilder()
                .server(transportServer) // Use jetty WebSocket-servlet based transport
                .discoverModules()       // Discover auto-generated endpoint modules
                .resolver(ServiceResolvers
                        .builder()
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

#### Java client example

```java
    RxClient rxClient = RxClient.forClient(JettyWebSocketRxTransport.builder().buildClient());
    SayHelloEndpoint sayHelloClient = rxClient.connect(uri).resolve(SayHelloEndpoint_RxClient.class);
    sayHelloClient
            .sayHello("Alice")
            .test()
            .awaitDone(1000, TimeUnit.MILLISECONDS)
            .assertValueCount(1)
            .assertValue("Hello, Alice");
```

### Component diagram

![Diagram](http://www.plantuml.com/plantuml/png/3Op13G8n30J_LmKKGFpdTQsOd9MBb3ZHsXQKZr-hrOpcD6Tup74ykzOHbmJ6utRVzYO2VqlhViSdJ52NkgYNkYRPwFFLX6647tQboUO2oX8btqlJjBBp7wVebVi7)
