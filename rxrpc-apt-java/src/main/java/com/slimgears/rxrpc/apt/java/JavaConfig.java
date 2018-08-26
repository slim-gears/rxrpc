/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

public class JavaConfig {
    @AutoService(ConfigProvider.class)
    public static class Provider implements ConfigProvider {
        @Override
        public void apply(Properties properties) {
            ConfigProviders
                    .loadFromResource("/java-config.properties")
                    .apply(properties);
        }
    }

    @Inject @Named("rxrpc.java.client") boolean generateClient;
    @Inject @Named("rxrpc.java.server") boolean generateServer;
    @Inject @Named("rxrpc.java.autoservice") boolean applyAutoService;
}
