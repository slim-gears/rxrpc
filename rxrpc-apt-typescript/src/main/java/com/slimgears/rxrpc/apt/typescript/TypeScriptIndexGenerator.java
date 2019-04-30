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
        TypeScriptIndexGenerator.rxRpcJsVersionOption,
        TypeScriptIndexGenerator.rxJsVersionOption,
        TypeScriptIndexGenerator.angularVersionOption,
        TypeScriptIndexGenerator.typeScriptVersionOption
})
public class TypeScriptIndexGenerator implements CodeGenerationFinalizer {
    static final String generateNgModuleOption = TypeScriptEndpointGenerator.generateNgModuleOption;
    static final String ngModuleNameOption = "rxrpc.ts.ngmodule.name";
    static final String generateNpmOption = "rxrpc.ts.npm";
    static final String npmVersionOption = "rxrpc.ts.npm.version";
    static final String npmDescriptionOption = "rxrpc.ts.npm.description";
    static final String npmAuthorOption = "rxrpc.ts.npm.author";
    static final String npmNameOption = "rxrpc.ts.npm.name";
    static final String rxRpcJsVersionOption = "rxrpc.ts.npm.deps.rxrpcjs.version";
    static final String rxJsVersionOption = "rxrpc.ts.npm.deps.rxjs.version";
    static final String angularVersionOption = "rxrpc.ts.npm.deps.angular.version";
    static final String typeScriptVersionOption = "rxrpc.ts.npm.deps.typescript.version";

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
                    .variable("rxJsVersion", context.option(rxJsVersionOption))
                    .variable("angularVersion", context.option(angularVersionOption))
                    .variable("typeScriptVersion", context.option(typeScriptVersionOption))
                    .write(TypeScriptUtils.fileWriter("package.json"));
            TemplateEvaluator
                    .forResource("tsconfig.json.vm")
                    .write(TypeScriptUtils.fileWriter("tsconfig.json"));
        }

        TypeScriptUtils.fileWriter("index.ts").accept(GeneratedClassTracker.current().generateIndex());
    }
}
