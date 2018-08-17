/**
 *
 */
package com.slimgears.rxrpc.client;

import io.reactivex.Single;

import java.lang.reflect.InvocationTargetException;

public class EndpointFactories {
    public static RxClient.EndpointFactory constructorFactory() {
        return EndpointFactories::createEndpoint;
    }

    private static <T> T createEndpoint(Class<T> clientClass, Single<RxClient.Session> session) {
        try {
            return clientClass.getConstructor(Single.class).newInstance(session);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
