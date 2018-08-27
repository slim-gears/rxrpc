/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.CodeGenerationFinalizer;

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
                    .variable("ngModuleName", context.option("rxrpc.ts.ngmodule.name"))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "module.ts"));
            index.append("\n");
            index.append("export * from './module';\n");
        }

        if (context.options().containsKey("rxrpc.ts.npm")) {
            TemplateEvaluator
                    .forResource("/package.json.vm")
                    .variable("generateNgModule", generateNgModule)
                    .variable("npmModuleVersion", context.option("rxrpc.ts.npm.version"))
                    .variable("npmModuleDescription", context.option("rxrpc.ts.npm.description"))
                    .variable("npmModuleAuthor", context.option("rxrpc.ts.npm.author"))
                    .variable("npmModuleName", context.option("rxrpc.ts.npm.name"))
                    .variable("ngRxRpcVersion", context.option("rxrpc.ts.ngrxrpc.version"))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "package.json"));
            TemplateEvaluator
                    .forResource("/tsconfig.json.vm")
                    .write(TypeScriptUtils.fileWriter(context.environment(), "tsconfig.json"));
        }

        TypeScriptUtils.writeFile(context.environment(), "index.ts", index.toString());
    }
}
