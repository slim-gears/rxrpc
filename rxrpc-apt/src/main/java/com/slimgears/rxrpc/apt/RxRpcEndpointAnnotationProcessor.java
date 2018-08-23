package com.slimgears.rxrpc.apt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.Environment;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.rxrpc.apt.util.StreamUtils;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcEndpoint")
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor {
    private final Collection<EndpointGenerator> endpointGenerators;
    private final Collection<CodeGenerationFinalizer> finalizers;
    private final Collection<DataClassGenerator> dataClassGenerators;
    private final Collection<Name> processedClasses = new HashSet<>();

    public RxRpcEndpointAnnotationProcessor() {
        endpointGenerators = ServiceProviders.loadServices(EndpointGenerator.class);
        finalizers = ServiceProviders.loadServices(CodeGenerationFinalizer.class);
        dataClassGenerators = ServiceProviders.loadServices(DataClassGenerator.class);
    }

    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        log.info("Processing type: {}", typeElement.getQualifiedName());
        EndpointGenerator.Context context = createContext(annotationType, typeElement);
        endpointGenerators.forEach(cg -> cg.generate(context));
        return true;
    }

    private void generateDataType(TypeElement typeElement) {
        TypeInfo typeInfo = TypeInfo.of(typeElement);
        if (processedClasses.contains(typeElement.getQualifiedName()) ||
                TemplateUtils.isKnownAsyncType(typeInfo) ||
                Environment.instance().isIgnoredType(typeInfo)) {
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
                .filter(element -> !ElementUtils.hasAnnotation(element, JsonIgnore.class))
                .filter(element -> !ElementUtils.hasAnnotation(element, Override.class))
                .map(PropertyInfo::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(builder::property);

        DataClassGenerator.Context context = builder.build();
        dataClassGenerators.forEach(g -> g.generate(context));
    }

    protected void onComplete() {
        CodeGenerationFinalizer.Context context = CodeGenerationFinalizer.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(processingEnv.getElementUtils().getTypeElement(Object.class.getName()))
                .environment(processingEnv)
                .build();

        finalizers.forEach(f -> f.generate(context));
        finalizers.clear();
    }

    protected EndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        DeclaredType declaredType = (DeclaredType)typeElement.asType();

        Collection<MethodInfo> methods = ElementUtils.toDeclaredTypeStream(typeElement)
                .flatMap(ElementUtils::getHierarchy)
                .flatMap(ElementUtils::getMethods)
                .map(method -> ensureReferencedTypesGenerated(method, declaredType))
                .map(methodElement -> MethodInfo.create(methodElement, declaredType))
                .collect(Collectors.toList());

        return EndpointGenerator.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(typeElement)
                .environment(processingEnv)
                .meta(typeElement.getAnnotation(RxRpcEndpoint.class))
                .addMethods(methods)
                .build();
    }

    private ExecutableElement ensureReferencedTypesGenerated(ExecutableElement element, DeclaredType declaredType) {
        ExecutableType executableType = (ExecutableType) Environment.instance().types().asMemberOf(declaredType, element);

        Stream.of(
                ElementUtils.getReferencedTypes(element),
                executableType.getParameterTypes()
                        .stream()
                        .flatMap(ElementUtils::getReferencedTypeParams)
                        .flatMap(ElementUtils::toTypeElement),
                Stream.of(executableType.getReturnType())
                        .flatMap(ElementUtils::getReferencedTypeParams)
                        .flatMap(ElementUtils::toTypeElement))
                .flatMap(StreamUtils.self())
                .peek(type -> log.debug("Found referenced type: {}", type.getQualifiedName()))
                .filter(ElementUtils::isUnknownType)
                .forEach(this::generateDataType);
        return element;
    }
}
