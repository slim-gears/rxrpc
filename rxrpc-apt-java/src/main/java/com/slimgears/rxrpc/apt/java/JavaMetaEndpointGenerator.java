/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.MetaEndpointGenerator;
import com.slimgears.rxrpc.apt.data.MetaEndpointInfo;

@AutoService(MetaEndpointGenerator.class)
public class JavaMetaEndpointGenerator implements MetaEndpointGenerator {
    @Override
    public void generate(Context context) {
        context.endpoints().forEach(ep -> generateInstantiation(context, ep));
    }

    private void generateInstantiation(Context context, MetaEndpointInfo metaEndpoint) {
        ImportTracker importTracker = ImportTracker.create(context.sourceClass().packageName());
        context.log().info("Generating class: {}", metaEndpoint.targetType().fullName());

        TemplateEvaluator
                .forResource("endpoint-meta.java.vm")
                .variables(context)
                .variables(metaEndpoint)
                .variable("javaUtils", new JavaUtils())
                .variable("endpointName", metaEndpoint.name())
                .variable("endpointMeta", metaEndpoint)
                .apply(JavaUtils.imports(importTracker))
                .write(JavaUtils.fileWriter(context.environment(), metaEndpoint.targetType()));
    }
}
