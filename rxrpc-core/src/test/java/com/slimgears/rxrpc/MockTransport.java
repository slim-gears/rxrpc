/**
 *
 */
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

class MockTransport implements RxTransport.Server, RxTransport.Client {
    private final Subject<RxTransport> connections = BehaviorSubject.create();

    @Override
    public Observable<RxTransport> connections() {
        return connections;
    }

    @Override
    public Single<RxTransport> connect(URI uri) {
        Subject<String> clientIncoming = PublishSubject.create();
        Subject<String> serverIncoming = PublishSubject.create();

        RxTransport clientTransport = transportFor(clientIncoming, serverIncoming);
        RxTransport serverTransport = transportFor(serverIncoming, clientIncoming);

        connections.onNext(serverTransport);
        return Single.just(clientTransport);
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
