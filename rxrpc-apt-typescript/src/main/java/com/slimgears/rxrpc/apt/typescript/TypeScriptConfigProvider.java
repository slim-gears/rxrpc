package com.slimgears.rxrpc.apt.typescript;


import com.google.auto.service.AutoService;
import com.slimgears.util.guice.ConfigProvider;
import com.slimgears.util.guice.ConfigProviders;

import java.util.Properties;

@AutoService(ConfigProvider.class)
public class TypeScriptConfigProvider implements ConfigProvider {
    @Override
    public void apply(Properties properties) {
        ConfigProviders
                .loadFromResource("/rxrpc-apt-typescript.properties")
                .apply(properties);
    }
}
