package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.slimgears.rxrpc.core.RxRpcEndpoint", "javax.annotation.Generated"})
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor<EndpointGenerator, EndpointGenerator.Context> {
    private final Collection<CodeGenerationFinalizer> finalizers = new ArrayList<>();
    private final Collection<DataClassGenerator> dataClassGenerators = new ArrayList<>();
    private final Collection<Name> processedClasses = new HashSet<>();

    public RxRpcEndpointAnnotationProcessor() {
        super(EndpointGenerator.class);
        ServiceLoader.load(CodeGenerationFinalizer.class, getClass().getClassLoader()).forEach(finalizers::add);
        ServiceLoader.load(DataClassGenerator.class, getClass().getClassLoader()).forEach(dataClassGenerators::add);
    }

    public RxRpcEndpointAnnotationProcessor(EndpointGenerator... generators) {
        super(generators);
    }

    private void generateDataType(TypeElement typeElement) {
        if (processedClasses.contains(typeElement.getQualifiedName()) || TemplateUtils.isKnownAsyncType(TypeInfo.of(typeElement))) {
            return;
        }
        processedClasses.add(typeElement.getQualifiedName());

        log.info("Generating from: {}", typeElement.getQualifiedName());

        ElementUtils
                .getReferencedTypes(typeElement)
                .forEach(this::generateDataType);

        DataClassGenerator.Context.Builder builder = DataClassGenerator.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(typeElement)
                .environment(processingEnv);

        typeElement.getEnclosedElements()
                .stream()
                .map(PropertyInfo::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(builder::property);

        DataClassGenerator.Context context = builder.build();
        dataClassGenerators.forEach(g -> g.generate(context));
    }

    @Override
    protected boolean processAnnotation(TypeElement annotationType, RoundEnvironment roundEnv) {
        if (!annotationType.getQualifiedName().toString().equals(Generated.class.getName())) {
            return super.processAnnotation(annotationType, roundEnv);
        }

        if (roundEnv.getElementsAnnotatedWith(annotationType)
                .stream()
                .map(element -> element.getAnnotation(Generated.class))
                .flatMap(g -> Stream.of(g.value()))
                .anyMatch(g -> g.equals(getClass().getName()))) {
            onFinalize(annotationType);
        }

        return false;
    }

    private void onFinalize(TypeElement annotationType) {
        CodeGenerationFinalizer.Context context = CodeGenerationFinalizer.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(annotationType)
                .environment(processingEnv)
                .build();
        finalizers.forEach(f -> f.generate(context));
        finalizers.clear();
    }

    @Override
    protected EndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        Collection<MethodInfo> methods = ElementUtils.getHierarchy(typeElement)
                .flatMap(t -> t.getEnclosedElements().stream())
                .filter(el -> el.getAnnotation(RxRpcMethod.class) != null)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .map(this::ensureReferencedTypesGenerated)
                .map(MethodInfo::of)
                .collect(Collectors.toList());

        return EndpointGenerator.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(typeElement)
                .environment(processingEnv)
                .meta(typeElement.getAnnotation(RxRpcEndpoint.class))
                .addMethods(methods)
                .build();
    }

    private ExecutableElement ensureReferencedTypesGenerated(ExecutableElement element) {
        ElementUtils
                .getReferencedTypes(element)
                .filter(ElementUtils::isUnknownType)
                .forEach(this::generateDataType);
        return element;
    }
}
