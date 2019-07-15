package com.slimgears.rxrpc.apt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
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
import com.slimgears.util.stream.Optionals;
import com.slimgears.util.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcEndpoint")
public class RxRpcEndpointAnnotationProcessor extends AbstractAnnotationProcessor {
    private final static Logger log = LoggerFactory.getLogger(RxRpcEndpointAnnotationProcessor.class);
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
            endpointGenerators.forEach(cg -> {
                try {
                    cg.generate(context);
                } catch (Throwable e) {
                    Environment.instance().messager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }
            });
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

        DeclaredType declaredType = MoreTypes.asDeclared(typeElement.asType());

        Stream.concat(
                propertyElementsFromTypeElement(typeElement)
                        .map(el -> propertyTypeFromElement(declaredType, el))
                        .flatMap(ElementUtils::getReferencedTypeParams)
                        .flatMap(ElementUtils::toTypeElement),
                ElementUtils.getHierarchy(typeElement))
                .filter(ElementUtils::isUnknownType)
                .forEach(this::generateDataType);

        DataClassGenerator.Context.Builder builder = DataClassGenerator.Context.builder()
                .processorClass(getClass())
                .sourceTypeElement(typeElement)
                .environment(processingEnv);

        Collection<PropertyInfo> allProperties = inheritedAndLocalPropertyElements(typeElement)
                .map(element -> PropertyInfo.fromElement(declaredType, element))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());

        typeElement.getEnclosedElements()
                .stream()
                .filter(ElementUtils::isNotStatic)
                .filter(ElementUtils::isPublic)
                .filter(element -> !ElementUtils.hasAnnotation(element, Override.class))
                .filter(element -> !ElementUtils.hasAnnotation(element, JsonIgnore.class))
                .filter(element -> !element.getModifiers().contains(Modifier.DEFAULT))
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

    private TypeMirror propertyTypeFromElement(DeclaredType type, Element element) {
        return Optionals
                .or(
                        () -> Optional.of(element)
                                .flatMap(Optionals.ofType(ExecutableElement.class))
                                .map(el -> Environment.instance().types().asMemberOf(type, el))
                                .map(MoreTypes::asExecutable)
                                .map(ExecutableType::getReturnType),
                        () -> Optional.of(element)
                                .flatMap(Optionals.ofType(VariableElement.class))
                                .map(el -> Environment.instance().types().asMemberOf(type, el))
                )
                .orElseThrow(() -> new IllegalArgumentException("Element kind " + element.getKind() + " is not supported as a property element"));
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<? extends Element> propertyElementsFromTypeElement(TypeElement typeElement) {
        return Stream.concat(
                typeElement
                        .getEnclosedElements()
                        .stream()
                        .flatMap(Streams.ofType(ExecutableElement.class))
                        .filter(element -> ElementUtils.isUnknownType(MoreElements.asType(element.getEnclosingElement())))
                                .filter(ElementUtils::isNotStatic)
                                .filter(ElementUtils::isPublic)
                                .filter(element -> element.getParameters().isEmpty())
                                .filter(element -> !ElementUtils.hasAnnotation(element, JsonIgnore.class))
                                .filter(element ->
                                        !element.getModifiers().contains(Modifier.DEFAULT) ||
                                        ElementUtils.hasAnnotation(element, JsonProperty.class))
                                .filter(element -> !element.getReturnType().toString().equals(Void.class.getName()))
                                .filter(element -> !element.getReturnType().toString().equals(void.class.getName())),
                typeElement
                        .getEnclosedElements()
                        .stream()
                        .flatMap(Streams.ofType(VariableElement.class))
                        .filter(ElementUtils::isNotStatic)
                        .filter(ElementUtils::isPublic)
                        .filter(element -> !ElementUtils.hasAnnotation(element, JsonIgnore.class)));
    }

    private Stream<? extends Element> inheritedAndLocalPropertyElements(TypeElement typeElement) {
        return inheritedAndLocalPropertyElements(typeElement.asType(), Sets.newHashSet(), Sets.newHashSet());
    }

    private Stream<? extends Element> inheritedAndLocalPropertyElements(TypeMirror type, Set<TypeMirror> visitedInterfaces, Set<String> visitedProperties) {
        if (!(type instanceof DeclaredType) || type.toString().equals(Object.class.getName())) {
            return Stream.empty();
        }

        TypeElement typeElement = MoreTypes.asTypeElement(type);

        return (typeElement.asType().toString().equals(Object.class.getName()))
                ? Stream.empty()
                : Stream.of(
                propertyElementsFromTypeElement(typeElement)
                        .filter(element -> PropertyInfo
                                .fromElement(element)
                                .map(PropertyInfo::name)
                                .map(visitedProperties::add)
                                .orElse(false)),
                inheritedAndLocalPropertyElements(typeElement.getSuperclass(), visitedInterfaces, visitedProperties),
                typeElement.getInterfaces()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(visitedInterfaces::add)
                        .flatMap(iface -> inheritedAndLocalPropertyElements(iface, visitedInterfaces, visitedProperties)))
                .flatMap(Function.identity());
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

    @SuppressWarnings("UnstableApiUsage")
    private EndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
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
        moduleContextBuilder.addModule(moduleName, TypeInfo.of(declaredType), annotation);

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
                .orElse("");
    }

    private String endpointNameFromClass(TypeElement typeElement) {
        return TemplateUtils.camelCaseToDash(typeElement.getSimpleName().toString());
    }

    private ExecutableElement ensureReferencedTypesGenerated(ExecutableElement element, DeclaredType declaredType) {
        ExecutableType executableType = (ExecutableType) Environment.instance().types().asMemberOf(declaredType, element);
        if (executableType.getParameterTypes().stream().anyMatch(ElementUtils::hasErrors)) {
            delayProcessing(executableType.getParameterTypes().stream()
                    .filter(ElementUtils::hasErrors)
                    .collect(Collectors.toList()));
        }

        List<TypeMirror> errorTypes = Stream.concat(
                Stream.of(executableType.getReturnType()),
                executableType.getParameterTypes().stream())
                .filter(ElementUtils::hasErrors)
                .collect(Collectors.toList());

        if (!errorTypes.isEmpty()) {
            delayProcessing(errorTypes);
        }

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
