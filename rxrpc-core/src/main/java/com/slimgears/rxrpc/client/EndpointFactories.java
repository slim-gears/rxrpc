/**
 *
 */
package com.slimgears.rxrpc.client;

import com.slimgears.util.reflect.TypeToken;

public class EndpointFactories {
    public static RxClient.EndpointFactory constructorFactory() {
        return EndpointFactories::createEndpoint;
    }

    private static <T> T createEndpoint(TypeToken<T> clientToken, RxClient.Session session) {
        return clientToken
                .constructor(RxClient.Session.class)
                .newInstance(session);
    }
}
