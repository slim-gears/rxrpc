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
                    .variable("ngModuleName", context
                            .options()
                            .getOrDefault("rxrpc.ts.ngmodule.name", "RxRpcGeneratedClientModule"))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "module.ts"));
        }

        if (context.options().containsKey("rxrpc.ts.npm")) {
            TemplateEvaluator
                    .forResource("/package.json.vm")
                    .variable("generateNgModule", generateNgModule)
                    .variable("npmModuleVersion", context.option("rxrpc.ts.npm.version", "1.0.0"))
                    .variable("npmModuleDescription", context.option("rxrpc.ts.npm.description", ""))
                    .variable("npmModuleAuthor", context.option("rxrpc.ts.npm.author", "RxRpc Generated module"))
                    .variable("npmModuleName", context.option("rxrpc.ts.npm.name", "rxrpc-generated-client"))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "package.json"));
        }

        TypeScriptUtils.writeIndex(context.environment());
    }
}
