/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.CodeGenerationFinalizer;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;

import javax.inject.Inject;

@AutoService(CodeGenerationFinalizer.class)
public class TypeScriptIndexGenerator implements CodeGenerationFinalizer {
    private final TypeScriptUtils typeScriptUtils;
    private final TypeScriptConfig config;

    @Inject
    public TypeScriptIndexGenerator(TypeScriptUtils typeScriptUtils, TypeScriptConfig config) {
        this.typeScriptUtils = typeScriptUtils;
        this.config = config;
    }

    @Override
    public void generate(Context context) {
        StringBuilder index = new StringBuilder(typeScriptUtils.generateIndex());
        if (config.generateNgModule) {
            TemplateEvaluator
                    .forResource("/typescript-ngmodule.ts.vm")
                    .variables(context)
                    .variable("classes", typeScriptUtils.getGeneratedEndpoints())
                    .variable("ngModuleName", config.ngModuleName)
                    .write(typeScriptUtils.fileWriter(context.environment(), "module.ts"));
            index.append("\n");
            index.append("export * from './module';\n");
        }

        if (config.generateNpm) {
            TemplateEvaluator
                    .forResource("/package.json.vm")
                    .variable("generateNgModule", config.generateNgModule)
                    .variable("npmModuleVersion", config.npmVersion)
                    .variable("npmModuleDescription", config.npmDescription)
                    .variable("npmModuleAuthor", config.npmAuthor)
                    .variable("npmModuleName", config.npmName)
                    .variable("ngRxRpcVersion", config.ngRxRpcVersion)
                    .write(typeScriptUtils.fileWriter(context.environment(), "package.json"));
            TemplateEvaluator
                    .forResource("/tsconfig.json.vm")
                    .write(typeScriptUtils.fileWriter(context.environment(), "tsconfig.json"));
        }

        TypeScriptUtils.writeFile(context.environment(), "index.ts", index.toString());
    }
}
