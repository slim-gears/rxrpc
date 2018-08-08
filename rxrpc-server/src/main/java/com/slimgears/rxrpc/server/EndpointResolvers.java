/**
 *
 */
package com.slimgears.rxrpc.server;

public class EndpointResolvers {
    public static EndpointResolver defaultConstructorResolver() {
        return new EndpointResolver() {
            @Override
            public <T> T resolve(Class<T> cls) {
                try {
                    return cls.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
