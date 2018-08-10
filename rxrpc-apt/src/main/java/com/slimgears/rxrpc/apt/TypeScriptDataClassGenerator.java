/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.EnumInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.*;

@AutoService(DataClassGenerator.class)
public class TypeScriptDataClassGenerator implements DataClassGenerator {
    @Override
    public void generate(Context context) {
        String className = context.sourceTypeElement().getQualifiedName().toString();
        ImportTracker importTracker = ImportTracker.create(TypeInfo.packageName(className));

        String filename = TemplateUtils.instance.camelCaseToDash(context.sourceClass().simpleName()) + ".ts";

        evaluator(context)
                .variable("tsUtils", new TypeScriptUtils(importTracker))
                .variables(context)
                .apply(TypeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(context.environment(), filename));
    }

    private TemplateEvaluator evaluator(Context context) {
        if (ElementUtils.isEnum(context.sourceTypeElement())) {
            return TemplateEvaluator.forResource("/typescript-enum.ts.vm")
                    .variable("enum", EnumInfo.of(context.sourceTypeElement()));
        } else {
            return TemplateEvaluator.forResource("/typescript-data-class.ts.vm");
        }
    }
}
