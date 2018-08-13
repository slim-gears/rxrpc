/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.ImportTracker;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.apt.util.TypeScriptUtils;

@AutoService(EndpointGenerator.class)
public class TypeScriptEndpointGenerator implements EndpointGenerator {
    @Override
    public void generate(Context context) {
        String className = context.sourceTypeElement().getQualifiedName().toString() + "Client";
        ImportTracker importTracker = ImportTracker.create(TypeInfo.packageName(className));

        String filename = TemplateUtils.camelCaseToDash(context.sourceClass().simpleName()) + ".ts";

        TypeScriptUtils.addGeneratedClass(
                TypeInfo.of(context.sourceTypeElement()),
                TypeInfo.of(context.sourceClass().simpleName()));

        TemplateEvaluator.forResource("/typescript-client.ts.vm")
                .variable("targetClass", TypeInfo.of(className))
                .variable("tsUtils", new TypeScriptUtils(importTracker))
                .variables(context)
                .apply(TypeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(context.environment(), filename));
    }
}
