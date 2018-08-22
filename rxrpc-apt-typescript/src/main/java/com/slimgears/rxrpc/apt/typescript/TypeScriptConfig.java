/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.util.ConfigProvider;
import com.slimgears.rxrpc.apt.util.ConfigProviders;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

class TypeScriptConfig {
    @AutoService(ConfigProvider.class)
    public static class Provider implements ConfigProvider {
        @Override
        public void apply(Properties properties) {
            ConfigProviders
                    .loadFromResource("/typescript-config.properties")
                    .apply(properties);
        }
    }

    @Inject @Named("rxrpc.ts.npm") boolean generateNpm;
    @Inject @Named("rxrpc.ts.npm.version") String npmVersion;
    @Inject @Named("rxrpc.ts.npm.description") String npmDescription;
    @Inject @Named("rxrpc.ts.npm.author") String npmAuthor;
    @Inject @Named("rxrpc.ts.npm.name") String npmName;

    @Inject @Named("rxrpc.ts.ngrxrpc.version") String ngRxRpcVersion;

    @Inject @Named("rxrpc.ts.ngmodule") boolean generateNgModule;
    @Inject @Named("rxrpc.ts.ngmodule.name") String ngModuleName;
}
