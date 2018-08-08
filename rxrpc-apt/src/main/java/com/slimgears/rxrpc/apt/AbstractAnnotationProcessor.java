package com.slimgears.rxrpc.apt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;

public class AbstractAnnotationProcessor extends AbstractProcessor {
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
            try {
                if (element instanceof TypeElement && ! processType(annotationType, (TypeElement)element)) {
                    return false;
                }
                else if (element instanceof ExecutableElement && !processMethod(annotationType, (ExecutableElement)element)) {
                    return false;
                } else if (element instanceof VariableElement && !processField(annotationType, (VariableElement)element)) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    protected boolean processAnnotation(Class annotationType, RoundEnvironment roundEnv) {
        return processAnnotation(
                processingEnv.getElementUtils().getTypeElement(annotationType.getCanonicalName()),
                roundEnv);
    }

    protected boolean processType(TypeElement annotationType, TypeElement typeElement) throws Exception { return false; }
    protected boolean processMethod(TypeElement annotationType, ExecutableElement methodElement) throws Exception { return false; }
    protected boolean processField(TypeElement annotationType, VariableElement variableElement) throws Exception { return false; }
}
