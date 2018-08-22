package com.slimgears.rxrpc.apt.typescript;


import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.util.ConfigProvider;
import com.slimgears.rxrpc.apt.util.ConfigProviders;

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
