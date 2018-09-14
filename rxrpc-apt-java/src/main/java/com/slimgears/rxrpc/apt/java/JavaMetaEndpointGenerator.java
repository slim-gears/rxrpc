/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.slimgears.apt.data.AnnotationInfo;
import com.slimgears.apt.data.AnnotationValueInfo;
import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.MethodInfo;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.JavaUtils;
import com.slimgears.apt.util.NameTemplateUtils;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.MetaEndpointGenerator;
import com.slimgears.rxrpc.apt.data.MetaEndpointInfo;
import com.slimgears.rxrpc.core.RxRpcEndpoint;
import com.slimgears.rxrpc.core.RxRpcGenerate;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.slimgears.util.stream.Streams.ofType;

@AutoService(MetaEndpointGenerator.class)
public class JavaMetaEndpointGenerator implements MetaEndpointGenerator {
    @Override
    public void generate(Context context) {
        context.endpoints().forEach(ep -> generateInstantiation(context, ep));
    }

    private void generateInstantiation(Context context, MetaEndpointInfo metaEndpoint) {
        ImportTracker importTracker = ImportTracker.create(context.sourceClass().packageName());
        context.log().info("Generating class: {}", metaEndpoint.targetType().fullName());

        TypeMirror[] typeArgMirrors = ElementUtils.typeMirrorsFromAnnotation(metaEndpoint.meta(), RxRpcGenerate.Endpoint::params);
        TypeInfo[] typeArgs = Stream.of(typeArgMirrors).map(TypeInfo::of).toArray(TypeInfo[]::new);

        TypeElement sourceType = context.sourceTypeElement();

        Preconditions.checkArgument(
                typeArgs.length == sourceType.getTypeParameters().size(),
                "@RxRpcGenerate.Endpoint parameters number mismatch.");

        String nameTemplate = context.meta().className();
        TypeInfo sourceClass = TypeInfo.of(sourceType);
        Types types = Environment.instance().types();
        DeclaredType superTypeMirror = types.getDeclaredType(sourceType, typeArgMirrors);
        MethodInfo[] constructors = sourceType
                .getEnclosedElements()
                .stream()
                .filter(ElementUtils.ofKind(ElementKind.CONSTRUCTOR))
                .flatMap(ofType(ExecutableElement.class))
                .map(ee -> MethodInfo.create(ee, superTypeMirror))
                .toArray(MethodInfo[]::new);

        TypeInfo superClass = TypeInfo.builder()
                .name(sourceType.getQualifiedName().toString())
                .typeParams(typeArgs)
                .build();

        String packageName = sourceClass.packageName();
        String targetTypeName = NameTemplateUtils.getTypeName(nameTemplate, sourceType, typeArgs);
        TypeInfo targetClass = TypeInfo.of(packageName + "." + targetTypeName);

        ImmutableMap<String, TypeInfo> typeParams = IntStream
                .range(0, typeArgs.length)
                .boxed()
                .collect(ImmutableMap.toImmutableMap(
                        i -> sourceClass.typeParams().get(i).typeName(),
                        i -> typeArgs[i]));

        ImmutableList<MappedConstructorInfo> mappedConstructors = Stream.of(constructors)
                .map(c -> MappedConstructorInfo.builder()
                        .superConstructor(c)
                        .classParams(typeParams)
                        .params(c.params().stream().filter(p -> !p.hasAnnotation(RxRpcGenerate.ClassParam.class)))
                        .build())
                .collect(ImmutableList.toImmutableList());

        boolean generateClient = mergeAnnotationInfo(context, metaEndpoint, RxRpcEndpoint::generateClient, Boolean::logicalAnd);
        boolean generateServer = mergeAnnotationInfo(context, metaEndpoint, RxRpcEndpoint::generateServer, Boolean::logicalAnd);

        AnnotationInfo.Builder rxRpcEndpointBuilder = AnnotationInfo
                .builder()
                .type(RxRpcEndpoint.class);

        if (!generateClient) {
            rxRpcEndpointBuilder.value(AnnotationValueInfo.ofPrimitive("generateClient", false));
        }

        if (!generateServer) {
            rxRpcEndpointBuilder.value(AnnotationValueInfo.ofPrimitive("generateServer", false));
        }

        if (!metaEndpoint.name().isEmpty()) {
            rxRpcEndpointBuilder.valuesBuilder().add(AnnotationValueInfo.ofPrimitive("value", metaEndpoint.name()));
        }

        if (!context.moduleName().isEmpty()) {
            rxRpcEndpointBuilder.valuesBuilder().add(AnnotationValueInfo.ofPrimitive("moduleName", context.moduleName()));
        }

        if (!metaEndpoint.options().isEmpty()) {
            rxRpcEndpointBuilder.valuesBuilder()
                    .add(AnnotationValueInfo.ofArray(
                            "options",
                            TypeInfo.of(String[].class),
                            metaEndpoint
                                    .options()
                                    .entrySet()
                                    .stream()
                                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                                    .map(AnnotationValueInfo.Value::ofPrimitive)
                                    .toArray(AnnotationValueInfo.Value[]::new)));
        }

        TemplateEvaluator
                .forResource("endpoint-meta.java.vm")
                .variables(context)
                .variables(metaEndpoint)
                .variable("isInterface", ElementUtils.isInterface(sourceType))
                .variable("sourceClass", sourceClass)
                .variable("targetClass", targetClass)
                .variable("superClass", superClass)
                .variable("mappedConstructors", mappedConstructors)
                .variable("typeParams", typeParams)
                .variable("javaUtils", new JavaUtils())
                .variable("endpointName", metaEndpoint.name())
                .variable("endpointMeta", metaEndpoint)
                .variable("annotation", rxRpcEndpointBuilder.build())
                .apply(JavaUtils.imports(importTracker))
                .write(JavaUtils.fileWriter(context.environment(), metaEndpoint.targetType()));
    }

    private <T> T mergeAnnotationInfo(Context context, MetaEndpointInfo endpointInfo, Function<RxRpcEndpoint, T> getter, BinaryOperator<T> merger) {
        T fromContext = getter.apply(context.meta().annotation());
        T fromEndpoint = getter.apply(endpointInfo.meta().annotation());
        return merger.apply(fromContext, fromEndpoint);
    }
}
