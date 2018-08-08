/**
 *
 */
package com.slimgears.rxrpc.client;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

public class EndpointFactories {
    public static RxClient.EndpointFactory defaultConstructorFactory() {
        return new RxClient.EndpointFactory() {
            @Override
            public <T> T create(Class<T> clientClass, Future<RxClient.Session> session) {
                try {
                    return clientClass.getConstructor(Future.class).newInstance(session);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
