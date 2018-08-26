package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.slimgears.util.stream.Safe;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;

@AutoValue
public abstract class Environment {
    private final static ThreadLocal<Environment> instance = new ThreadLocal<>();

    public abstract ProcessingEnvironment processingEnvironment();
    public abstract RoundEnvironment roundEnvironment();

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
        return new AutoValue_Environment(processingEnvironment, roundEnvironment);
    }

    public static Environment instance() {
        return Optional.ofNullable(instance.get()).orElseThrow(() -> new RuntimeException("Environment was not set"));
    }

    public static Safe.Closable withEnvironment(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
        Environment prev = instance.get();
        instance.set(create(processingEnvironment, roundEnvironment));
        return () -> instance.set(prev);
    }
}
