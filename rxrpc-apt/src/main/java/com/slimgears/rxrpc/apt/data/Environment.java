package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.apt.util.ConfigProvider;
import com.slimgears.rxrpc.apt.util.ConfigProviders;
import com.slimgears.rxrpc.apt.util.Safe;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@AutoValue
public abstract class Environment {
    private final static String configOptionName = "rxrpc.config";
    private final static String ignoredTypesOptionName = "rxrpc.ignoredTypes";
    private final static ThreadLocal<Environment> instance = new ThreadLocal<>();

    public abstract ProcessingEnvironment processingEnvironment();
    public abstract RoundEnvironment roundEnvironment();
    public abstract Properties properties();
    protected abstract Predicate<TypeInfo> ignoredTypePredicate();

    public boolean isIgnoredType(TypeInfo typeInfo) {
        return ignoredTypePredicate().test(typeInfo);
    }

    public Messager messager() {
        return processingEnvironment().getMessager();
    }

    public Types types() {
        return processingEnvironment().getTypeUtils();
    }

    public Elements elements() {
        return processingEnvironment().getElementUtils();
    }

    private static Environment create(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
        Properties properties = ConfigProviders.create(
                ConfigProviders.loadFromResource("/rxrpc-apt.properties"),
                ConfigProviders.fromServiceLoader(),
                loadFromExternalConfig(processingEnvironment),
                loadFromOptions(processingEnvironment));

        Predicate<TypeInfo> ignoredTypesFilter = Optional
                .ofNullable(properties.getProperty(ignoredTypesOptionName))
                .map(Environment::typeFilterFromWildcards)
                .orElse(t -> false);

        return new AutoValue_Environment(
                processingEnvironment,
                roundEnvironment,
                properties,
                ignoredTypesFilter);
    }

    public static Environment instance() {
        return Optional.ofNullable(instance.get()).orElseThrow(() -> new RuntimeException("Environment was not set"));
    }

    public static Safe.SafeClosable withEnvironment(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
        Environment prev = instance.get();
        instance.set(create(processingEnvironment, roundEnvironment));
        return () -> instance.set(prev);

    }

    private static ConfigProvider loadFromExternalConfig(ProcessingEnvironment processingEnvironment) {
        return Optional
                .ofNullable(processingEnvironment.getOptions().get(configOptionName))
                .map(ConfigProviders::loadFromFile)
                .orElse(ConfigProviders.empty);
    }

    private static ConfigProvider loadFromOptions(ProcessingEnvironment processingEnvironment) {
        return props -> processingEnvironment
                .getOptions()
                .forEach((key, value) -> props.put(key, Optional
                        .ofNullable(value)
                        .map(Object::toString)
                        .orElse("true")));
    }

    private static Predicate<TypeInfo> typeFilterFromWildcards(String wildcards) {
        return Optional
                .ofNullable(wildcards)
                .map(w -> w.split(","))
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .map(String::trim)
                .map(Environment::fromWildcard)
                .map(Pattern::asPredicate)
                .map(Environment::fromStringPredicate)
                .reduce(Predicate::or)
                .orElse(t -> false);
    }

    private static Predicate<TypeInfo> fromStringPredicate(Predicate<String> predicate) {
        return type -> predicate.test(type.name());
    }

    private static Pattern fromWildcard(String wildcard) {
        String regex = "^" + wildcard
                .replace(".", "\\.")
                .replace("?", ".")
                .replace("*", ".*") + "$";
        return Pattern.compile(regex);
    }
}
