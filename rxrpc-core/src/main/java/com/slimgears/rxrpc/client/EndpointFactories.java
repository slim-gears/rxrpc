/**
 *
 */
package com.slimgears.rxrpc.client;

import com.slimgears.util.reflect.TypeToken;

import java.util.Optional;

public class EndpointFactories {
    public static RxClient.EndpointFactory constructorFactory() {
        return new RxClient.EndpointFactory() {
            @Override
            public <T> T create(TypeToken<T> clientToken, RxClient.Session session) {
                return findConstructor(clientToken)
                        .map(c -> c.newInstance(session))
                        .orElse(null);
            }

            @Override
            public boolean canCreate(TypeToken<?> clientToken) {
                return findConstructor(clientToken).isPresent();
            }

            private <T> Optional<TypeToken.Constructor<T>> findConstructor(TypeToken<T> token) {
                return Optional.ofNullable(token.constructor(RxClient.Session.class));
            }
        };
    }
}
