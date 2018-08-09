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

@AutoService(DataClassGenerator.class)
public class TypeScriptDataClassGenerator implements DataClassGenerator {
    @Override
    public void generate(Context context) {
        String className = context.sourceTypeElement().getQualifiedName().toString();
        ImportTracker importTracker = ImportTracker.create(TypeInfo.packageName(className));

        String filename = TemplateUtils.instance.camelCaseToDash(context.sourceClass().simpleName()) + ".ts";
        TemplateEvaluator.forResource("/typescript-data-class.ts.vm")
                .variable("tsUtils", new TypeScriptUtils(importTracker))
                .variables(context)
                .apply(TypeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(context.environment(), filename));
    }
}
