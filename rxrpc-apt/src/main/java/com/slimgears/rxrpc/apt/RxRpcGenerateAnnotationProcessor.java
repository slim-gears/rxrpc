/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.MetaEndpointInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.AbstractAnnotationProcessor;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcGenerate")
public class RxRpcGenerateAnnotationProcessor extends AbstractAnnotationProcessor {
    private final Collection<MetaEndpointGenerator> metaEndpointGenerators;

    public RxRpcGenerateAnnotationProcessor() {
        metaEndpointGenerators = ServiceProviders.loadServices(MetaEndpointGenerator.class);
    }

    protected boolean processType(TypeElement annotationType, TypeElement typeElement) {
        log.info("Processing type: {} ({} generators)", typeElement.getQualifiedName(), metaEndpointGenerators.size());
        MetaEndpointGenerator.Context context = createContext(annotationType, typeElement);
        metaEndpointGenerators.forEach(cg -> cg.generate(context));
        return true;
    }

    protected MetaEndpointGenerator.Context createContext(TypeElement annotationType, TypeElement typeElement) {
        validateType(typeElement);

        RxRpcGenerate meta = typeElement.getAnnotation(RxRpcGenerate.class);
        MetaEndpointGenerator.Context.Builder builder = MetaEndpointGenerator.Context
                .builder()
                .environment(processingEnv)
                .sourceTypeElement(typeElement)
                .processorClass(getClass())
                .meta(meta);

        TypeInfo typeInfo = TypeInfo.of(typeElement);

        Stream.of(meta.value())
                .map(m -> createEndpointMetaInfo(typeInfo, meta, m))
                .forEach(builder::endpoint);

        return builder.build();
    }

    private MetaEndpointInfo createEndpointMetaInfo(TypeInfo typeInfo, RxRpcGenerate meta, RxRpcGenerate.Endpoint endpointMeta) {
        Map<String, TypeInfo> typeParams = new HashMap<>();

        TypeInfo[] typeArgs = ElementUtils.typesFromAnnotation(endpointMeta, RxRpcGenerate.Endpoint::params);
        IntStream.range(0, typeArgs.length).forEach(i -> typeParams.put(typeInfo.typeParams().get(i).name(), typeArgs[i]));

        StringSubstitutor substitutor = new StringSubstitutor(
                str -> Objects.requireNonNull(typeParams.get(str)).simpleName(),
                "${", "}", StringSubstitutor.DEFAULT_ESCAPE);

        String targetTypeSimpleName = Optional
                .of(endpointMeta.className())
                .filter(n -> !n.isEmpty())
                .orElseGet(() -> substitutor.replace(meta.className()));

        String endpointName = Optional
                .of(endpointMeta.endpointName())
                .filter(n -> !n.isEmpty())
                .orElseGet(() -> substitutor.replace(meta.endpointName()));

        String packageName = typeInfo.packageName();
        TypeInfo superType = TypeInfo.builder()
                .name(typeInfo.name())
                .typeParams(typeArgs)
                .build();

        TypeInfo targetType = TypeInfo.of(packageName + "." + targetTypeSimpleName);

        return MetaEndpointInfo.builder()
                .superType(superType)
                .targetType(targetType)
                .meta(endpointMeta)
                .name(endpointName)
                .build();
    }

    private void validateType(TypeElement typeElement) {
        require(ElementUtils.isInterface(typeElement), "Annotated type should be interface");
        require(!typeElement.getTypeParameters().isEmpty(), "Annotated type should have one or more type parameters");
        RxRpcGenerate meta = typeElement.getAnnotation(RxRpcGenerate.class);
        require(meta.value().length > 0, "Meta endpoint instantiations are not defined");
        Stream.of(meta.value()).forEach(epm -> validateEndpoint(typeElement, meta, epm));
    }

    private void validateEndpoint(TypeElement typeElement, RxRpcGenerate meta, RxRpcGenerate.Endpoint endpointMeta) {
        if (endpointMeta.className().isEmpty()) {
            require(!meta.className().isEmpty(), "Class name/template is not defined for some endpoint instantiations");
            validateNameTemplate(meta.className(), typeElement);
        }

        if (endpointMeta.endpointName().isEmpty()) {
            require(!meta.endpointName().isEmpty(), "Endpoint name/template is not defined for some endpoint instantiations");
            validateNameTemplate(meta.endpointName(), typeElement);
        }

        require(ElementUtils.typesFromAnnotation(endpointMeta, RxRpcGenerate.Endpoint::params).length == typeElement.getTypeParameters().size(), "Parameter number mismatch");
    }

    private void validateNameTemplate(String template, TypeElement typeElement) {
        Set<String> namesFromTypeParams = typeElement.getTypeParameters()
                .stream()
                .map(tp -> tp.getSimpleName().toString())
                .collect(Collectors.toSet());

        Set<String> varNames = getVarNames(template);

        require(varNames.size() == typeElement.getTypeParameters().size(), "Template variable names number mismatch");
        varNames.forEach(n -> require(namesFromTypeParams.contains(n), "Template variable name " + n + " does not correspond to type parameter"));
    }

    private static void require(boolean condition, String errorMsg) {
        if (!condition) {
            throw new RuntimeException("Incorrent @" + RxRpcGenerate.class.getSimpleName() + " usage: " + errorMsg);
        }
    }

    private Set<String> getVarNames(String template) {
        Set<String> names = new HashSet<>();
        StringSubstitutor substitutor = createSubstitutor(name -> { names.add(name); return name; });
        substitutor.replace(template);
        return names;
    }

    private static StringSubstitutor createSubstitutor(StringLookup lookup) {
        return new StringSubstitutor(lookup, "${", "}", StringSubstitutor.DEFAULT_ESCAPE);
    }
}
