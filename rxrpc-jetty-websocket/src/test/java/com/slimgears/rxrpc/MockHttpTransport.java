package com.slimgears.rxrpc;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import com.slimgears.rxrpc.jettyhttp.JettyHttpAttributes;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class MockHttpTransport implements RxTransport.Server, RxTransport.Client {
    private final Subject<RxTransport> connections = BehaviorSubject.create();
    private final AtomicReference<RxTransport> clientTransport = new AtomicReference<>();
    private final AtomicReference<RxTransport> serverTransport = new AtomicReference<>();
    private final AtomicReference<Queue<String>> messageQueue = new AtomicReference<>(new LinkedList<>());
    private final AtomicReference<Disposable> disconnectSubscription = new AtomicReference<>(Disposables.empty());
    private final Subject<String> ServerOutgoing = BehaviorSubject.create();
    Subject<String> clientIncoming = PublishSubject.create();
    private final Disposable outgoingSubscription;

    public MockHttpTransport() {
        this.outgoingSubscription = ServerOutgoing
                .subscribe(this::onMessage);
    }

    public void close() {
        disconnectSubscription.get().dispose();
        outgoingSubscription.dispose();
    }

    @Override
    public Observable<RxTransport> connections() {
        return connections;
    }

    @Override
    public Single<RxTransport> connect(URI uri) {
        Subject<String> serverIncoming = PublishSubject.create();

        this.clientTransport.set(transportFor(clientIncoming, serverIncoming));
        this.serverTransport.set(transportFor(serverIncoming, ServerOutgoing));

        connections.onNext(serverTransport.get());
        return Single.just(clientTransport.get());
    }

    private RxTransport transportFor(Observable<String> incoming, Observer<String> outgoing) {
        return new RxTransport() {
            @Override
            public Emitter<String> outgoing() {
                return Emitters.fromObserver(outgoing);
            }

            @Override
            public Observable<String> incoming() {
                return incoming;
            }
        };
    }

    private void onMessage(String message) {
        messageQueue.get().add(message);
    }

    private Iterable<String> dequePendingMessages() {
        Disposable previousSubscription = disconnectSubscription.getAndSet(Completable
                .timer(JettyHttpAttributes.ServerKeepAliveTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .subscribe(this::close));
        previousSubscription.dispose();

        if (messageQueue.get().isEmpty()) {
            return Collections.emptyList();
        }
        return messageQueue.getAndSet(new LinkedList<>());
    }

    public Completable doPoll() {
        return Completable.create(emitter -> {
            Iterable<String> messages = dequePendingMessages();
            messages.forEach(clientIncoming::onNext);
            emitter.onComplete();
        });
    }
}