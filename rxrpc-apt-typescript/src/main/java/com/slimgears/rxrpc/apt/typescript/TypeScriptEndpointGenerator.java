/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.EndpointGenerator;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.ImportTracker;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(EndpointGenerator.class)
public class TypeScriptEndpointGenerator implements EndpointGenerator {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptEndpointGenerator.class);

    @Override
    public void generate(Context context) {
        String className = context.sourceTypeElement().getQualifiedName().toString() + "Client";
        ImportTracker importTracker = ImportTracker.create(TypeInfo.packageName(className));

        log.debug("Generating code for source type: {}", context.sourceTypeElement().getQualifiedName());
        log.debug("Target class name: {}", className);
        TypeInfo targetClass = TypeInfo.of(TypeInfo.of(className).simpleName());

        String filename = TemplateUtils.camelCaseToDash(targetClass.name()) + ".ts";
        log.debug("Target file name: {}", filename);

        TypeScriptUtils.addGeneratedClass(
                TypeInfo.of(context.sourceTypeElement()),
                targetClass);

        TypeScriptUtils.addGeneratedEndpoint(targetClass);

        TemplateEvaluator.forResource("/typescript-client.ts.vm")
                .variable("targetClass", targetClass)
                .variable("tsUtils", new TypeScriptUtils(importTracker))
                .variables(context)
                .apply(TypeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(context.environment(), filename));
    }
}
