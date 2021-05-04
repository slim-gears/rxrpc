package com.slimgears.rxrpc.jetty.http;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import com.slimgears.util.stream.Streams;
import io.reactivex.Completable;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class JettyHttpRxTransportServer implements RxTransport {
    private final Subject<String> outgoingSubject = BehaviorSubject.create();
    private final Emitter<String> outgoing = Emitters.fromObserver(outgoingSubject);
    private final Subject<String> incoming = BehaviorSubject.create();
    private final AtomicReference<Queue<String>> messageQueue = new AtomicReference<>(new LinkedList<>());
    private final Disposable outgoingSubscription;
    private final AtomicReference<Disposable> disconnectSubscription = new AtomicReference<>(Disposables.empty());
    private final Duration keepAliveTimeout;

    public JettyHttpRxTransportServer(Duration keepAliveTimeout) {
        this.outgoingSubscription = outgoingSubject
                .subscribe(this::onMessage,
                        incoming::onError);
        this.keepAliveTimeout = keepAliveTimeout;
    }

    private void onMessage(String message) {
        messageQueue.get().add(message);
    }

    public Iterable<String> dequePendingMessages() {
        Disposable previousSubscription = disconnectSubscription.getAndSet(Completable
                .timer(keepAliveTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .subscribe(this::close));
        previousSubscription.dispose();

        if (messageQueue.get().isEmpty()) {
            return Collections.emptyList();
        }
        return messageQueue.getAndSet(new LinkedList<>());
    }

    @Override
    public Emitter<String> outgoing() {
        return outgoing;
    }

    @Override
    public Subject<String> incoming() {
        return incoming;
    }

    @Override
    public void close() {
        this.disconnectSubscription.get().dispose();
        this.outgoingSubscription.dispose();
        incoming().onComplete();
    }

    public static class Builder {
        private Duration keepAliveTimeout = JettyHttpAttributes.ServerKeepAliveTimeout;

        public Builder keepAliveTimeout(Duration keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
            return this;
        }

        public Server build() {
            return new Server(keepAliveTimeout);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Server extends HttpServlet implements RxTransport.Server {
        private final Subject<RxTransport> connections = BehaviorSubject.create();
        private final Map<String, JettyHttpRxTransportServer> transportMap = new ConcurrentHashMap<>();
        private final Duration keepAliveTimeout;

        private Server(Duration keepAliveTimeout){
            this.keepAliveTimeout = keepAliveTimeout;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String reqURI = req.getRequestURI();
            String reqType = reqURI.substring(reqURI.lastIndexOf('/') + 1);
            String clientId = req.getHeader(JettyHttpAttributes.ClientIdAttribute);
            String message = req.getReader().readLine();
            switch (reqType)
            {
                case "connect":
                    doConnect(resp);
                    break;
                case "disconnect":
                    doDisconnect(clientId, resp);
                    break;
                case "message":
                    doMessage(clientId, message, resp);
                    break;
                case "polling":
                    doPoll(clientId, resp);
                    break;
                default:
                    resp.setStatus(HttpStatus.BAD_REQUEST_400);
            }
        }

        private JettyHttpRxTransportServer transportById(String clientId) {
            return Optional.ofNullable(transportMap.get(clientId))
                    .orElseThrow(() -> new RuntimeException("Could not find client " + clientId));
        }

        private void doPoll(String clientId, HttpServletResponse response) {
            Iterable<String> messages = transportById(clientId).dequePendingMessages();
            try {
                String body = Streams.fromIterable(messages).collect(Collectors.joining(",\n", "[", "]"));
                response.getWriter().write(body);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void doConnect(HttpServletResponse response) {
            String id = new BigInteger(128, new SecureRandom()).toString(64);
            JettyHttpRxTransportServer transport = new JettyHttpRxTransportServer(keepAliveTimeout);
            transportMap.put(id, transport);
            connections.onNext(transport);
            response.addHeader(JettyHttpAttributes.ClientIdAttribute, id);
        }

        private void doDisconnect(String clientId, HttpServletResponse response) {
            JettyHttpRxTransportServer transport = transportMap.remove(clientId);
            if ( transport == null) {
                response.setStatus(HttpStatus.BAD_REQUEST_400);
            } else {
                transport.close();
            }
        }

        private void doMessage(String clientId, String message, HttpServletResponse response) {
            transportById(clientId).incoming().onNext(message);
            doPoll(clientId, response);
        }

        @Override
        public Observable<RxTransport> connections() {
            return connections;
        }
    }
}
