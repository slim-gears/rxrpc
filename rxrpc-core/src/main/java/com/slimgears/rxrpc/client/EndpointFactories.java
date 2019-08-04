/**
 *
 */
package com.slimgears.rxrpc.client;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.slimgears.util.reflect.TypeTokens;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class EndpointFactories {
    public static RxClient.EndpointFactory constructorFactory() {
        return new RxClient.EndpointFactory() {
            @Override
            public <T> T create(TypeToken<T> clientToken, RxClient.Session session) {
                return findConstructor(clientToken)
                        .map(c -> {
                            try {
                                return c.invoke(null, session);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
            }

            @Override
            public boolean canCreate(TypeToken<?> clientToken) {
                return findConstructor(clientToken).isPresent();
            }

            private <T> Optional<Invokable<T, T>> findConstructor(TypeToken<T> token) {
                try {
                    return Optional.of(token.constructor(token.getRawType().getConstructor(RxClient.Session.class)));
                } catch (NoSuchMethodException e) {
                    return Optional.empty();
                }
            }
        };
    }
}
