package com.slimgears.rxrpc.jetty.http;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import com.slimgears.util.stream.Streams;
import io.reactivex.Completable;
import io.reactivex.Emitter;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class JettyHttpRxTransportServer {
    private final static Logger log = LoggerFactory.getLogger(JettyHttpRxTransportServer.class);
    private final static SecureRandom random = new SecureRandom();

    static class Connection implements RxTransport {
        private final Subject<String> outgoingSubject = BehaviorSubject.create();
        private final Emitter<String> outgoing = Emitters.fromObserver(outgoingSubject);
        private final Subject<String> incoming = BehaviorSubject.create();
        private final AtomicReference<Queue<String>> messageQueue = new AtomicReference<>(new LinkedList<>());
        private final Subject<String> messageSubject = PublishSubject.create();
        private final Disposable outgoingSubscription;
        private final AtomicReference<Disposable> disconnectSubscription = new AtomicReference<>(Disposables.empty());
        private final Duration keepAliveTimeout;
        private final Duration observeDuration;
        private final List<Runnable> onCloseListeners = new ArrayList<>();
        private final AtomicBoolean inObserve = new AtomicBoolean();
        private final CompletableSubject closeSubject = CompletableSubject.create();

        public Connection(Duration keepAliveTimeout, Duration observeDuration) {
            this.outgoingSubscription = outgoingSubject
                    .subscribe(this::onMessage,
                            incoming::onError);
            this.keepAliveTimeout = keepAliveTimeout;
            this.observeDuration = observeDuration;
        }

        Connection onClose(Runnable onClose) {
            this.onCloseListeners.add(onClose);
            return this;
        }

        private synchronized void onMessage(String message) {
            messageQueue.get().add(message);
            messageSubject.onNext(message);
        }

        public synchronized Iterable<String> dequePendingMessages() {
            restartKeepAliveTimer();
            if (messageQueue.get().isEmpty()) {
                return Collections.emptyList();
            }
            return messageQueue.getAndSet(new LinkedList<>());
        }

        private void stopKeepAliveTimer() {
            this.disconnectSubscription.getAndSet(Disposables.empty()).dispose();
        }

        private void restartKeepAliveTimer() {
            Disposable previousSubscription = disconnectSubscription.getAndSet(Completable
                    .timer(keepAliveTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .subscribe(this::close));
            previousSubscription.dispose();
        }

        private void onObserve(HttpServletRequest request, HttpServletResponse response) {
            var asyncContext = request.startAsync();
            try {
                var writer = asyncContext.getResponse().getWriter();
                observePendingMessages()
                        .observeOn(Schedulers.io())
                        .take(observeDuration.toMillis(), TimeUnit.MILLISECONDS)
                        .takeUntil(closeSubject.andThen(Observable.just(new Object())))
                        .doOnNext(msg -> {
                            writer.print(msg);
                            writer.flush();
                            asyncContext.getResponse().flushBuffer();
                        })
                        .ignoreElements()
                        .subscribe(
                                asyncContext::complete,
                                e -> {
                                    log.error("Error:", e);
                                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                                    asyncContext.complete();
                                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void onPoll(HttpServletResponse response) {
            Iterable<String> messages = dequePendingMessages();
            try {
                String body = Streams.fromIterable(messages).collect(Collectors.joining(",\n", "[", "]"));
                response.getWriter().write(body);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void onMessage(String message, HttpServletResponse response) {
            if (!message.isEmpty()) {
                this.incoming.onNext(message);
            }
            if (!inObserve.get()) {
                this.onPoll(response);
            }
        }

        @Override
        public void close() {
            this.disconnectSubscription.get().dispose();
            this.outgoingSubscription.dispose();
            incoming().onComplete();
            this.onCloseListeners.forEach(Runnable::run);
        }

        @Override
        public Emitter<String> outgoing() {
            return outgoing;
        }

        @Override
        public Subject<String> incoming() {
            return incoming;
        }

        public synchronized Observable<String> observePendingMessages() {
            return Observable.fromIterable(dequePendingMessages())
                    .concatWith(messageSubject.flatMapMaybe(n -> Optional
                                    .ofNullable(messageQueue.get().poll())
                                    .map(Maybe::just)
                                    .orElseGet(Maybe::empty))
                    .doOnSubscribe(d -> {
                        stopKeepAliveTimer();
                        inObserve.set(true);
                    })
                    .doFinally(() -> {
                        restartKeepAliveTimer();
                        inObserve.set(false);
                    }));
        }
    }

    public static class Builder {
        private Duration keepAliveTimeout = JettyHttpAttributes.ServerKeepAliveTimeout;
        private Duration longPollingDuration = JettyHttpAttributes.LongPollingDuration;

        public Builder keepAliveTimeout(Duration keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
            return this;
        }

        public Builder longPollingDuration(Duration longPollingDuration) {
            this.longPollingDuration = longPollingDuration;
            return this;
        }

        public Server build() {
            return new Server(keepAliveTimeout, longPollingDuration);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Server extends HttpServlet implements RxTransport.Server {
        private final Subject<RxTransport> connections = BehaviorSubject.create();
        private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
        private final Duration keepAliveTimeout;
        private final Duration longPollingDuration;

        private Server(Duration keepAliveTimeout, Duration longPollingDuration) {
            this.keepAliveTimeout = keepAliveTimeout;
            this.longPollingDuration = longPollingDuration;
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String reqUri = req.getRequestURI();
            String reqType = reqUri.substring(reqUri.lastIndexOf('/') + 1);

            if ("connect".equals(reqType)) {
                doConnect(resp);
                return;
            }

            String clientId = req.getHeader(JettyHttpAttributes.ClientIdAttribute);
            Connection connection = connectionMap.get(clientId);

            if (connection == null) {
                resp.setStatus(HttpStatus.UNAUTHORIZED_401);
                return;
            }


            try {
                switch (reqType) {
                    case "disconnect":
                        connection.close();
                        break;
                    case "message":
                        String message = req.getReader().lines().collect(Collectors.joining("\n"));
                        connection.onMessage(message, resp);
                        break;
                    case "polling":
                        connection.onPoll(resp);
                        break;
                    case "observe":
                        connection.onObserve(req, resp);
                        break;
                    default:
                        resp.setStatus(HttpStatus.BAD_REQUEST_400);
                }
            } catch (Throwable e) {
                log.error("Error while handling request to {}", reqUri, e);
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
        }

        private void doConnect(HttpServletResponse response) {
            String id = new BigInteger(128, random).toString(32);
            var connection = new Connection(keepAliveTimeout, longPollingDuration)
                    .onClose(() -> connectionMap.remove(id));

            connectionMap.put(id, connection);
            connections.onNext(connection);

            response.addHeader(JettyHttpAttributes.ClientIdAttribute, id);
        }

        @Override
        public Observable<RxTransport> connections() {
            return connections;
        }
    }
}
