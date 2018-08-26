/**
 *
 */
package com.slimgears.rxrpc.apt.internal;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;
import java.util.regex.Pattern;

import com.slimgears.util.guice.ConfigProvider;
import com.slimgears.util.guice.ConfigProviders;
import com.slimgears.util.guice.PropertyModules;
import com.slimgears.util.guice.TypeConversionModule;
import com.slimgears.util.stream.Patterns;

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

        install(Modules.override(new TypeConversionModule())
                        .with(TypeConversionModule.builder()
                                .isExactly(TypeInfo.class).convert(TypeInfo::of)
                                .isExactly(Pattern.class).convert(Patterns::fromWildcard)
                                .build()));

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
