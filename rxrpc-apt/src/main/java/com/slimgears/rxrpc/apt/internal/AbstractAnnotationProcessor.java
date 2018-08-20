package com.slimgears.rxrpc.apt.internal;

import com.slimgears.rxrpc.apt.data.Environment;
import com.slimgears.rxrpc.apt.util.LogUtils;
import com.slimgears.rxrpc.apt.util.Safe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;
import java.util.stream.Stream;

import static com.slimgears.rxrpc.apt.util.StreamUtils.ofType;

public abstract class AbstractAnnotationProcessor<G extends CodeGenerator<C>, C extends CodeGenerator.Context> extends AbstractProcessor {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Collection<G> codeGenerators = new ArrayList<>();

    protected AbstractAnnotationProcessor(Class<G> codeGeneratorClass) {
        ServiceLoader.load(codeGeneratorClass, getClass().getClassLoader()).forEach(codeGenerators::add);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try (LogUtils.SelfClosable ignored = LogUtils.applyLogging(processingEnv);
             Safe.SafeClosable envClosable = Environment.withEnvironment(processingEnv, roundEnv)) {
            onStart();
            boolean res = annotations
                    .stream()
                    .map(a -> processAnnotation(a, roundEnv))
                    .reduce(Boolean::logicalOr)
                    .orElse(false);
            onComplete();
            return res;
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    protected void onStart() {
    }

    protected void onComplete() {

    }

    protected boolean processAnnotation(TypeElement annotationType, RoundEnvironment roundEnv) {
        return roundEnv.getElementsAnnotatedWith(annotationType)
                .stream()
                .flatMap(e -> Stream
                        .of(
                                ofType(TypeElement.class, Stream.of(e)).map(_e -> processType(annotationType, _e)),
                                ofType(ExecutableElement.class, Stream.of(e)).map(_e -> processMethod(annotationType, _e)),
                                ofType(VariableElement.class, Stream.of(e)).map(_e -> processField(annotationType, _e)))
                        .flatMap(s -> s))
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    protected abstract C createContext(TypeElement annotationType, TypeElement typeElement);

    protected boolean processAnnotation(Class annotationType, RoundEnvironment roundEnv) {
        return processAnnotation(
                processingEnv.getElementUtils().getTypeElement(annotationType.getCanonicalName()),
                roundEnv);
    }

    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        log.info("Processing type: {}", typeElement.getQualifiedName());
        C context = createContext(annotationType, typeElement);
        codeGenerators.forEach(cg -> cg.generate(context));
        return true;
    }

    protected boolean processMethod(TypeElement annotationType, ExecutableElement methodElement) { return false; }
    protected boolean processField(TypeElement annotationType, VariableElement variableElement) { return false; }
}
