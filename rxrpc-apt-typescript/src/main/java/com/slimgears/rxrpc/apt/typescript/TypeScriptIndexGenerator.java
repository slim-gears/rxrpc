/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.CodeGenerationFinalizer;

import javax.annotation.processing.SupportedOptions;

@AutoService(CodeGenerationFinalizer.class)
@SupportedOptions({
        TypeScriptIndexGenerator.generateNgModuleOption,
        TypeScriptIndexGenerator.ngModuleNameOption,
        TypeScriptIndexGenerator.generateNpmOption,
        TypeScriptIndexGenerator.npmNameOption,
        TypeScriptIndexGenerator.npmVersionOption,
        TypeScriptIndexGenerator.npmAuthorOption,
        TypeScriptIndexGenerator.npmDescriptionOption,
        TypeScriptIndexGenerator.rxRpcJsVersionOption
})
public class TypeScriptIndexGenerator implements CodeGenerationFinalizer {
    static final String generateNgModuleOption = TypeScriptEndpointGenerator.generateNgModuleOption;
    static final String ngModuleNameOption = "rxrpc.ts.ngmodule.name";
    static final String generateNpmOption = "rxrpc.ts.npm";
    static final String npmVersionOption = "rxrpc.ts.npm.version";
    static final String npmDescriptionOption = "rxrpc.ts.npm.description";
    static final String npmAuthorOption = "rxrpc.ts.npm.author";
    static final String npmNameOption = "rxrpc.ts.npm.name";
    static final String rxRpcJsVersionOption = "rxrpc.ts.rxrpcjs.version";

    @Override
    public void generate(Context context) {
        boolean generateNgModule = context.hasOption(generateNgModuleOption);
        if (generateNgModule) {
            NgModuleGenerator
                    .create(context)
                    .name(context.option(ngModuleNameOption))
                    .addEndpoints(GeneratedClassTracker.current().generatedEndpoints())
                    .write();
        }

        if (context.hasOption(generateNpmOption)) {
            TemplateEvaluator
                    .forResource("package.json.vm")
                    .variable("generateNgModule", generateNgModule)
                    .variable("npmModuleVersion", context.option(npmVersionOption))
                    .variable("npmModuleDescription", context.option(npmDescriptionOption))
                    .variable("npmModuleAuthor", context.option(npmAuthorOption))
                    .variable("npmModuleName", context.option(npmNameOption))
                    .variable("rxRpcJsVersion", context.option(rxRpcJsVersionOption))
                    .write(TypeScriptUtils.fileWriter(context.environment(), "package.json"));
            TemplateEvaluator
                    .forResource("tsconfig.json.vm")
                    .write(TypeScriptUtils.fileWriter(context.environment(), "tsconfig.json"));
        }

        TypeScriptUtils.writeFile(context.environment(), "index.ts", GeneratedClassTracker.current().generateIndex());
    }
}
