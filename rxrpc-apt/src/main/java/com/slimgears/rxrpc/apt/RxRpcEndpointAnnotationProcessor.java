package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcEndpoint")
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor<EndpointGenerator, EndpointGenerator.Context> {
    public RxRpcEndpointAnnotationProcessor() {
        super(EndpointGenerator.class);
    }

    @Override
    protected EndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        Collection<MethodInfo> methods = typeElement
                .getEnclosedElements()
                .stream()
                .filter(el -> el.getAnnotation(RxRpcMethod.class) != null)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .map(MethodInfo::of)
                .collect(Collectors.toList());

        return EndpointGenerator.Context.builder()
                .sourceTypeElement(typeElement)
                .environment(processingEnv)
                .addMethods(methods)
                .meta(typeElement.getAnnotation(RxRpcEndpoint.class))
                .build();
    }
}
