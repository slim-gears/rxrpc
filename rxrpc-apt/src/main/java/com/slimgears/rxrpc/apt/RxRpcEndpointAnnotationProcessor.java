package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcEndpoint")
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor<EndpointGenerator, EndpointGenerator.Context> {
    private final Collection<CodeGenerationFinalizer> finalizers = new ArrayList<>();
    private final Collection<DataClassGenerator> dataClassGenerators = new ArrayList<>();
    private final Collection<Name> processedClasses = new HashSet<>();

    public RxRpcEndpointAnnotationProcessor() {
        super(EndpointGenerator.class);
        ServiceLoader.load(CodeGenerationFinalizer.class, getClass().getClassLoader()).forEach(finalizers::add);
        ServiceLoader.load(DataClassGenerator.class, getClass().getClassLoader()).forEach(dataClassGenerators::add);
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

    protected void onComplete() {
        CodeGenerationFinalizer.Context context = CodeGenerationFinalizer.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(processingEnv.getElementUtils().getTypeElement(Object.class.getName()))
                .environment(processingEnv)
                .build();

        finalizers.forEach(f -> f.generate(context));
        finalizers.clear();
    }

    @Override
    protected EndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        DeclaredType declaredType = (DeclaredType)typeElement.asType();
        Collection<MethodInfo> methods = ElementUtils.toDeclaredTypeStream(typeElement)
                .flatMap(ElementUtils::getHierarchy)
                .flatMap(ElementUtils::getMethods)
                .map(this::ensureReferencedTypesGenerated)
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

    private ExecutableElement ensureReferencedTypesGenerated(ExecutableElement element) {
        ElementUtils
                .getReferencedTypes(element)
                .filter(ElementUtils::isUnknownType)
                .forEach(this::generateDataType);
        return element;
    }
}
