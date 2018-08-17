/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc;

import com.slimgears.rxrpc.core.Transport;
import com.slimgears.rxrpc.core.util.Emitters;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import java.net.URI;

class MockTransport implements Transport.Server, Transport.Client {
    private final Subject<Transport> connections = BehaviorSubject.create();

    @Override
    public Observable<Transport> connections() {
        return connections;
    }

    @Override
    public Single<Transport> connect(URI uri) {
        Subject<String> clientIncoming = BehaviorSubject.create();
        Subject<String> serverIncoming = BehaviorSubject.create();

        Transport clientTransport = transportFor(clientIncoming, serverIncoming);
        Transport serverTransport = transportFor(serverIncoming, clientIncoming);

        connections.onNext(serverTransport);
        return Single.just(clientTransport);
    }

    private Transport transportFor(Observable<String> incoming, Observer<String> outgoing) {
        return new Transport() {
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
