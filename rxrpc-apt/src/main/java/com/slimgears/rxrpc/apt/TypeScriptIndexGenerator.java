/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.util.TypeScriptUtils;

@AutoService(CodeGenerationFinalizer.class)
public class TypeScriptIndexGenerator implements CodeGenerationFinalizer {
    @Override
    public void generate(Context context) {
        TemplateEvaluator
                .forResource("/typescript-ngmodule.ts.vm")
                .variable("classes", TypeScriptUtils.getGeneratedEndpoints())
                .write(TypeScriptUtils.fileWriter(context.environment(), "module.ts"));

        TemplateEvaluator
                .forResource("/package.json.vm")
                .write(TypeScriptUtils.fileWriter(context.environment(), "package.json"));

        String index = "export * from './module';\n" + TypeScriptUtils.generateIndex();
        TypeScriptUtils.writeFile(context.environment(), "index.ts", index);
    }
}
