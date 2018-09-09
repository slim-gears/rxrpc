/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.slimgears.apt.data.AnnotationInfo;
import com.slimgears.apt.data.AnnotationValueInfo;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.MetaEndpointGenerator;
import com.slimgears.rxrpc.apt.data.MetaEndpointInfo;
import com.slimgears.rxrpc.core.RxRpcEndpoint;

import javax.lang.model.element.AnnotationValue;

@AutoService(MetaEndpointGenerator.class)
public class JavaMetaEndpointGenerator implements MetaEndpointGenerator {
    @Override
    public void generate(Context context) {
        context.endpoints().forEach(ep -> generateInstantiation(context, ep));
    }

    private void generateInstantiation(Context context, MetaEndpointInfo metaEndpoint) {
        ImportTracker importTracker = ImportTracker.create(context.sourceClass().packageName());
        context.log().info("Generating class: {}", metaEndpoint.targetType().fullName());

        AnnotationInfo.Builder rxRpcEndpointBuilder = AnnotationInfo
                .builder()
                .type(RxRpcEndpoint.class);

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
                .variable("javaUtils", new JavaUtils())
                .variable("endpointName", metaEndpoint.name())
                .variable("endpointMeta", metaEndpoint)
                .variable("annotation", rxRpcEndpointBuilder.build())
                .apply(JavaUtils.imports(importTracker))
                .write(JavaUtils.fileWriter(context.environment(), metaEndpoint.targetType()));
    }
}
