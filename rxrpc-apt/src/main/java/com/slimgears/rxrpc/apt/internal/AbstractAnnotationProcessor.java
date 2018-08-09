package com.slimgears.rxrpc.apt.internal;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

public abstract class AbstractAnnotationProcessor<G extends CodeGenerator<C>, C extends CodeGenerator.Context> extends AbstractProcessor {
    private final Collection<G> codeGenerators = new ArrayList<>();

    protected AbstractAnnotationProcessor(Class<G> codeGeneratorClass) {
        ServiceLoader.load(codeGeneratorClass, getClass().getClassLoader()).forEach(codeGenerators::add);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotationType : annotations) {
            if (!processAnnotation(annotationType, roundEnv)) return false;
        }
        return true;
    }

        @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    protected boolean processAnnotation(TypeElement annotationType, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotationType);
        for (Element element : annotatedElements) {
            if (element instanceof TypeElement && ! processType(annotationType, (TypeElement)element)) {
                return false;
            }
            else if (element instanceof ExecutableElement && !processMethod(annotationType, (ExecutableElement)element)) {
                return false;
            } else if (element instanceof VariableElement && !processField(annotationType, (VariableElement)element)) {
                return false;
            }
        }
        return true;
    }

    protected abstract C createContext(TypeElement annotationType, TypeElement typeElement);

    protected boolean processAnnotation(Class annotationType, RoundEnvironment roundEnv) {
        return processAnnotation(
                processingEnv.getElementUtils().getTypeElement(annotationType.getCanonicalName()),
                roundEnv);
    }

    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        System.out.println("Processing type: " + typeElement.getQualifiedName().toString());
        C context = createContext(annotationType, typeElement);
        codeGenerators.forEach(cg -> cg.generate(context));
        return true;
    }

    protected boolean processMethod(TypeElement annotationType, ExecutableElement methodElement) { return false; }
    protected boolean processField(TypeElement annotationType, VariableElement variableElement) { return false; }
}
