/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.slimgears.apt.AbstractAnnotationProcessor;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.apt.util.NameTemplateUtils;
import com.slimgears.rxrpc.apt.data.MetaEndpointInfo;
import com.slimgears.rxrpc.apt.util.OptionsUtils;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import org.apache.commons.text.StringSubstitutor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.slimgears.rxrpc.core.RxRpcGenerate")
public class RxRpcGenerateAnnotationProcessor extends AbstractAnnotationProcessor {
    private final Collection<MetaEndpointGenerator> metaEndpointGenerators =
            ServiceProviders.loadServices(MetaEndpointGenerator.class);

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
                .moduleName(meta.annotation().module())
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
                .of(endpointMeta.annotation().value())
                .filter(n -> !n.isEmpty())
                .orElseGet(() -> substitutor.replace(meta.annotation().value()));

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
                .addOptions(OptionsUtils.toOptions(meta.annotation().options()))
                .addOptions(OptionsUtils.toOptions(endpointMeta.annotation().options()))
                .build();
    }

    private void validateType(TypeElement typeElement) {
        //checkArgument(ElementUtils.isInterface(typeElement), "Annotated type should be interface");
        checkArgument(!typeElement.getTypeParameters().isEmpty(), "Annotated type should have one or more type parameters");
        try {
            RxRpcGenerate meta = typeElement.getAnnotation(RxRpcGenerate.class);
            checkArgument(meta.value().length > 0, "Meta endpoint instantiations are not defined");
            Stream.of(meta.value()).forEach(epm -> validateEndpoint(typeElement, meta, epm));
        } catch (AnnotationTypeMismatchException exception) {
            delayProcessing("");
        }
    }

    private void validateEndpoint(TypeElement typeElement, RxRpcGenerate meta, RxRpcGenerate.Endpoint endpointMeta) {
        if (endpointMeta.className().isEmpty()) {
            checkArgument(!meta.className().isEmpty(), "Class name/template is not defined for some endpoint instantiations");
            NameTemplateUtils.validateNameTemplate(meta.className(), typeElement);
        }

        if (endpointMeta.annotation().value().isEmpty() && !meta.annotation().value().isEmpty()) {
            NameTemplateUtils.validateNameTemplate(meta.annotation().value(), typeElement);
        }

        TypeMirror[] typeMirrors = ElementUtils.typeMirrorsFromAnnotation(endpointMeta, RxRpcGenerate.Endpoint::params);
        if (Arrays.stream(typeMirrors).anyMatch(ElementUtils::hasErrors)) {
            delayProcessing(Arrays.stream(typeMirrors).filter(ElementUtils::hasErrors).collect(Collectors.toList()));
        }

        checkArgument(typeMirrors.length == typeElement.getTypeParameters().size(), "Parameter number mismatch");
    }

    @Override
    protected Stream<String> getAdditionalSupportedOptions() {
        return metaEndpointGenerators.stream().flatMap(c -> Stream.of(c.getSupportedOptions()));
    }
}
