package com.slimgears.rxrpc;

import com.slimgears.rxrpc.core.RxTransport;
import com.slimgears.rxrpc.core.util.Emitters;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class MockTransport implements RxTransport.Server, RxTransport.Client {
    private final Subject<RxTransport> connections = BehaviorSubject.create();
    private final AtomicReference<RxTransport> clientTransport = new AtomicReference<>();
    private final AtomicReference<RxTransport> serverTransport = new AtomicReference<>();

    @Override
    public Observable<RxTransport> connections() {
        return connections;
    }

    @Override
    public Single<RxTransport> connect(URI uri) {
        Subject<String> clientIncoming = PublishSubject.create();
        Subject<String> serverIncoming = PublishSubject.create();

        this.clientTransport.set(transportFor(clientIncoming, serverIncoming));
        this.serverTransport.set(transportFor(serverIncoming, clientIncoming));

        connections.onNext(serverTransport.get());
        return Single.just(clientTransport.get());
    }

    public RxTransport clientTransport() {
        return Objects.requireNonNull(this.clientTransport.get());
    }

    public RxTransport serverTransport() {
        return Objects.requireNonNull(this.serverTransport.get());
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
}
