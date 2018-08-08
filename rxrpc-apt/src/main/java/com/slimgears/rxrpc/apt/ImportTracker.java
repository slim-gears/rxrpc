/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportTracker {
    private final static String importsMagicWord = "`imports`";
    private final Collection<String> imports = new TreeSet<>();
    private final String selfPackageName;

    public static ImportTracker create(String selfPackageName) {
        return new ImportTracker(selfPackageName);
    }

    private ImportTracker(String selfPackageName) {
        this.selfPackageName = selfPackageName;
    }

    public String[] imports() {
        return this.imports.toArray(new String[imports.size()]);
    }

    public String use(TypeInfo typeInfo) {
        return simplify(typeInfo).fullName();
    }

    public String use(String cls) {
        TypeInfo typeInfo = TypeInfoParser.parse(cls);
        return use(typeInfo);
    }

    private TypeInfo simplify(TypeInfo typeInfo) {
        String packageName = typeInfo.packageName();
        if (!packageName.isEmpty() && !packageName.equals(selfPackageName)) {
            imports.add(packageName + "." + typeInfo.simpleName());
        }
        TypeInfo.Builder builder = TypeInfo.builder().name(typeInfo.simpleName());
        typeInfo.typeParams().stream().map(this::simplify).forEach(builder::typeParam);
        return builder.build();
    }

    @Override
    public String toString() {
        return importsMagicWord;
    }

    public Function<TemplateEvaluator, TemplateEvaluator> forJava() {
        return evaluator -> evaluator
                .variable("imports", this)
                .postProcess(PostProcessors.applyJavaImports(this));
    }

    String applyImports(String code, Function<String, String> importSubstitutor) {
        String importsStr = Stream.of(imports())
                .map(importSubstitutor)
                .collect(Collectors.joining("\n"));
        return code.replace(importsMagicWord, importsStr);
    }
}
