/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.internal;

import com.google.inject.AbstractModule;
import com.slimgears.rxrpc.apt.util.ConfigProvider;
import com.slimgears.rxrpc.apt.util.ConfigProviders;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;

public class EnvironmentModule extends AbstractModule {
    private final static String configOptionName = "rxrpc.config";
    private final ProcessingEnvironment processingEnvironment;

    public EnvironmentModule(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    protected void configure() {
        bind(ProcessingEnvironment.class).toInstance(processingEnvironment);
        bind(Types.class).toProvider(processingEnvironment::getTypeUtils);
        bind(Elements.class).toProvider(processingEnvironment::getElementUtils);
        bind(Filer.class).toProvider(processingEnvironment::getFiler);
        bind(Messager.class).toProvider(processingEnvironment::getMessager);

        install(PropertyModules.forProperties(
                ConfigProviders.loadFromResource("/config.properties"),
                ConfigProviders.fromServiceLoader(),
                loadFromExternalConfig(),
                loadFromOptions()));

        install(new TypeConversionModule());
        install(new ProcessingModule());
    }

    private ConfigProvider loadFromExternalConfig() {
        return Optional
                .ofNullable(processingEnvironment.getOptions().get(configOptionName))
                .map(ConfigProviders::loadFromFile)
                .orElse(ConfigProviders.empty);
    }

    private ConfigProvider loadFromOptions() {
        return props -> processingEnvironment
                .getOptions()
                .forEach((key, value) -> props.put(key, Optional
                        .ofNullable(value)
                        .map(Object::toString)
                        .orElse("true")));
    }
}
