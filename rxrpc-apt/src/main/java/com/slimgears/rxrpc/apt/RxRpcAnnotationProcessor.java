package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.EndpointContext;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.core.annotations.RxRpcEndpoint;
import com.slimgears.rxrpc.core.annotations.RxRpcMethod;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.annotations.RxRpcEndpoint")
public class RxRpcAnnotationProcessor extends AbstractAnnotationProcessor {
    private final Collection<EndpointGenerator> endpointGenerators = new ArrayList<>();

    public RxRpcAnnotationProcessor() {
        ServiceLoader.load(EndpointGenerator.class, getClass().getClassLoader()).forEach(endpointGenerators::add);
        System.out.println("Beginning endpoint processing (with " + endpointGenerators.size() + " generators)");
    }

    @Override
    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        System.out.println("Processing type: " + typeElement.getQualifiedName().toString());
        Collection<MethodInfo> methods = typeElement
                .getEnclosedElements()
                .stream()
                .filter(el -> el.getAnnotation(RxRpcMethod.class) != null)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .map(MethodInfo::of)
                .collect(Collectors.toList());

        RxRpcEndpoint endpointAnnotation = typeElement.getAnnotation(RxRpcEndpoint.class);
        EndpointContext context = EndpointContext.builder()
                .environment(processingEnv)
                .addMethods(methods)
                .sourceTypeElement(typeElement)
                .meta(endpointAnnotation)
                .utils(new TemplateUtils())
                .build();

        endpointGenerators.forEach(cg -> cg.generate(context));

        return true;
    }
}
