package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.core.annotations.RxRpcMethod;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.slimgears.slimrepo.core.annotations.Entity")
@AutoService(Processor.class)
public class RxRpcAnnotationProcessor extends AbstractAnnotationProcessor {
    @Override
    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        Collection<ExecutableElement> methods = typeElement
                .getEnclosedElements()
                .stream()
                .filter(el -> el.getAnnotation(RxRpcMethod.class) != null)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());

        String evaluated = TemplateEvaluator
                .forResource("/TypeScriptClient.ts.vm")
                .variable("className", typeElement.getSimpleName())
                .variable("methods", methods)
                .evaluate();

        return true;
    }
}
