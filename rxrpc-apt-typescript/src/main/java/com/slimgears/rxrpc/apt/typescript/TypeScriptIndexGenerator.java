/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.CodeGenerationFinalizer;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;

@AutoService(CodeGenerationFinalizer.class)
public class TypeScriptIndexGenerator implements CodeGenerationFinalizer {
    @Override
    public void generate(Context context) {
        boolean generateNgModule = context.options().containsKey("rxrpc.ts.ngmodule");
        if (generateNgModule) {
            TemplateEvaluator
                    .forResource("/typescript-ngmodule.ts.vm")
                    .variable("classes", TypeScriptUtils.getGeneratedEndpoints())
                    .write(TypeScriptUtils.fileWriter(context.environment(), "module.ts"));
        }

        if (context.options().containsKey("rxrpc.ts.npm")) {
            TemplateEvaluator
                    .forResource("/package.json.vm")
                    .variable("generateNgModule", generateNgModule)
                    .write(TypeScriptUtils.fileWriter(context.environment(), "package.json"));
        }

        TypeScriptUtils.writeIndex(context.environment());
    }
}
