package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcEndpoint")
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor {
    @Inject private Set<EndpointGenerator> endpointGenerators;
    @Inject private Set<DataClassGenerator> dataClassGenerators;
    @Inject private Set<CodeGenerationFinalizer> finalizers;
    @Inject @Named("rxrpc.ignoredTypes") private Set<Pattern> ignoredTypes;

    private final Collection<Name> processedClasses = new HashSet<>();

    @Override
    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        log.info("Processing type: {}", typeElement.getQualifiedName());
        EndpointGenerator.Context context = createContext(typeElement);
        endpointGenerators.forEach(cg -> cg.generate(context));
        return true;
    }

    private boolean isNotIgnoredType(TypeElement typeElement) {
        String name = typeElement.getQualifiedName().toString();
        boolean isIgnored = ignoredTypes
                .stream()
                .map(Pattern::asPredicate)
                .anyMatch(p -> p.test(name));
        return !isIgnored;
    }

    private void generateDataType(TypeElement typeElement) {
        if (processedClasses.contains(typeElement.getQualifiedName()) || !isNotIgnoredType(typeElement)) {
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
    }

    private EndpointGenerator.Context createContext(TypeElement typeElement) {
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
                .filter(this::isNotIgnoredType)
                .forEach(this::generateDataType);
        return element;
    }
}
