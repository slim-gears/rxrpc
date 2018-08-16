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
        StringBuilder index = new StringBuilder(TypeScriptUtils.generateIndex());
        if (generateNgModule) {
            TemplateEvaluator
                    .forResource("/typescript-ngmodule.ts.vm")
                    .variables(context)
                    .variable("classes", TypeScriptUtils.getGeneratedEndpoints())
                    .variable("ngModuleName", context
                            .options()
                            .getOrDefault("rxrpc.ts.ngmodule.name", "RxRpcGeneratedClientModule"))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "module.ts"));
            index.append("\n");
            index.append("export * from './module';\n");
        }

        if (context.options().containsKey("rxrpc.ts.npm")) {
            TemplateEvaluator
                    .forResource("/package.json.vm")
                    .variable("generateNgModule", generateNgModule)
                    .variable("npmModuleVersion", context.option("rxrpc.ts.npm.version", "1.0.0"))
                    .variable("npmModuleDescription", context.option("rxrpc.ts.npm.description", ""))
                    .variable("npmModuleAuthor", context.option("rxrpc.ts.npm.author", "RxRpc Generated module"))
                    .variable("npmModuleName", context.option("rxrpc.ts.npm.name", "rxrpc-generated-client"))
                    .variable("ngRxRpcVersion", context.option("rxrpc.ts.ngrxrpc.version", "0.2.4"))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "package.json"));
            TemplateEvaluator
                    .forResource("/tsconfig.json.vm")
                    .write(TypeScriptUtils.fileWriter(context.environment(), "tsconfig.json"));
        }

        TypeScriptUtils.writeFile(context.environment(), "index.ts", index.toString());
    }
}
