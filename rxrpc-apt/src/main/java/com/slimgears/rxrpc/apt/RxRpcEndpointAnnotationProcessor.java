package com.slimgears.rxrpc.apt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.slimgears.apt.AbstractAnnotationProcessor;
import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.MethodInfo;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.data.PropertyInfo;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;
import com.slimgears.rxrpc.apt.util.OptionsUtils;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcMethod;
import com.slimgears.util.stream.Streams;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcEndpoint")
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor {
    private final Collection<EndpointGenerator> endpointGenerators;
    private final Collection<CodeGenerationFinalizer> finalizers;
    private final Collection<DataClassGenerator> dataClassGenerators;
    private final Collection<ModuleGenerator> moduleGenerators;
    private final Collection<Name> processedClasses = new HashSet<>();
    private final ModuleGenerator.Context.Builder moduleContextBuilder = ModuleGenerator.Context
            .builder()
            .processorClass(getClass());

    public RxRpcEndpointAnnotationProcessor() {
        endpointGenerators = ServiceProviders.loadServices(EndpointGenerator.class);
        finalizers = ServiceProviders.loadServices(CodeGenerationFinalizer.class);
        dataClassGenerators = ServiceProviders.loadServices(DataClassGenerator.class);
        moduleGenerators = ServiceProviders.loadServices(ModuleGenerator.class);
    }

    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        log.info("Processing type: {}", typeElement.getQualifiedName());
        try (Environment ignored = Environment.instance()
                .toBuilder()
                .propertiesFrom(getOptions(typeElement))
                .build()) {
            EndpointGenerator.Context context = createContext(annotationType, typeElement);
            endpointGenerators.forEach(cg -> cg.generate(context));
        }
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

        Collection<PropertyInfo> allProperties =        typeElement.getInterfaces()
                .stream()
                .map(MoreTypes::asDeclared)
                .flatMap(iface -> MoreElements
                        .getLocalAndInheritedMethods(MoreElements.asType(iface.asElement()), Environment.instance().types(), Environment.instance().elements())
                        .stream()
                        .filter(element -> !ElementUtils.hasAnnotation(element, JsonIgnore.class))
                        .map(element -> PropertyInfo.fromElement(iface, element))
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .distinct()
                .collect(Collectors.toList());

        typeElement.getEnclosedElements()
                .stream()
                .filter(element -> !ElementUtils.hasAnnotation(element, Override.class))
                .filter(element -> !ElementUtils.hasAnnotation(element, JsonIgnore.class))
                .map(PropertyInfo::fromElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(prop -> {
                    builder.property(prop);
                    if (!allProperties.contains(prop)) {
                        allProperties.add(prop);
                    }
                });

        builder.allPropertiesBuilder().addAll(allProperties);
        DataClassGenerator.Context context = builder.build();
        dataClassGenerators.forEach(g -> g.generate(context));
    }

    protected void onComplete() {
        CodeGenerationFinalizer.Context context = CodeGenerationFinalizer.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(processingEnv.getElementUtils().getTypeElement(Object.class.getName()))
                .environment(processingEnv)
                .build();

        ModuleGenerator.Context moduleContext = moduleContextBuilder
                .environment(processingEnv)
                .build();
        moduleGenerators.forEach(g -> g.generate(moduleContext));

        finalizers.forEach(f -> f.generate(context));
        finalizers.clear();
    }

    protected EndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        DeclaredType declaredType = (DeclaredType)typeElement.asType();
        RxRpcEndpoint annotation = typeElement.getAnnotation(RxRpcEndpoint.class);

        Collection<MethodInfo> methods = MoreElements
                .getLocalAndInheritedMethods(
                        typeElement,
                        Environment.instance().types(),
                        Environment.instance().elements())
                .stream()
                .filter(isRxRpcMethod())
                .map(method -> ensureReferencedTypesGenerated(method, declaredType))
                .map(methodElement -> MethodInfo.create(methodElement, declaredType))
                .collect(Collectors.toList());

        String moduleName = getModuleName(typeElement);
        moduleContextBuilder.sourceTypeElement(annotationType);
        if (!Strings.isNullOrEmpty(moduleName)) {
            moduleContextBuilder.addModule(moduleName, TypeInfo.of(declaredType), annotation);
        }

        return EndpointGenerator.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(typeElement)
                .environment(processingEnv)
                .meta(annotation)
                .endpointName(getEndpointName(typeElement))
                .moduleName(moduleName)
                .addMethods(methods)
                .build();
    }

    private Predicate<ExecutableElement> isRxRpcMethod() {
        return element -> ElementUtils.getMethodAnnotation(element, RxRpcMethod.class).findAny().isPresent();
    }

    private Map<String, String> getOptions(TypeElement typeElement) {
        return Optional
                .ofNullable(typeElement.getAnnotation(RxRpcEndpoint.class))
                .map(RxRpcEndpoint::options)
                .map(OptionsUtils::toOptions)
                .orElseGet(ImmutableMap::of);
    }

    private String getEndpointName(TypeElement typeElement) {
        return Optional
                .ofNullable(typeElement.getAnnotation(RxRpcEndpoint.class))
                .map(RxRpcEndpoint::value)
                .filter(n -> !n.isEmpty())
                .orElseGet(() -> endpointNameFromClass(typeElement));
    }

    private String getModuleName(TypeElement typeElement) {
        return Optional
                .ofNullable(typeElement.getAnnotation(RxRpcEndpoint.class))
                .map(RxRpcEndpoint::module)
                .filter(n -> !n.isEmpty())
                .orElse(null);
    }

    private String endpointNameFromClass(TypeElement typeElement) {
        return TemplateUtils.camelCaseToDash(typeElement.getSimpleName().toString());
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
                .flatMap(Streams.self())
                .peek(type -> log.debug("Found referenced type: {}", type.getQualifiedName()))
                .filter(ElementUtils::isUnknownType)
                .forEach(this::generateDataType);
        return element;
    }

    @Override
    protected Stream<String> getAdditionalSupportedOptions() {
        return Stream.of(endpointGenerators, finalizers, dataClassGenerators, moduleGenerators)
                .flatMap(Collection::stream)
                .map(CodeGenerator::getSupportedOptions)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream);
    }
}
