/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.slimgears.rxrpc.apt.util.ConfigProvider;
import com.slimgears.rxrpc.apt.util.ConfigProviders;

import java.util.Properties;

public class PropertyModules {
    public static Module forProperties(ConfigProvider... loaders) {
        return forProperties(ConfigProviders.create(loaders));
    }

    public static Module forProperties(Properties properties) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Names.bindProperties(binder(), properties);
            }
        };
    }
}
