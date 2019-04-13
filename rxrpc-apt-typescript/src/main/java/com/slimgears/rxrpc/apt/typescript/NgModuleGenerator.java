package com.slimgears.rxrpc.apt.typescript;

import com.google.common.base.Preconditions;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.apt.util.TemplateUtils;
import com.slimgears.rxrpc.apt.internal.CodeGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class NgModuleGenerator {
    private final AtomicReference<String> name = new AtomicReference<>();
    private final Collection<TypeInfo> endpoints = new ArrayList<>();
    private final CodeGenerator.Context context;

    private NgModuleGenerator(CodeGenerator.Context context) {
        this.context = context;
    }

    public static NgModuleGenerator create(CodeGenerator.Context context) {
        return new NgModuleGenerator(context);
    }

    public NgModuleGenerator name(String name) {
        this.name.set(name);
        return this;
    }

    public NgModuleGenerator addEndpoints(Iterable<TypeInfo> endpoints) {
        endpoints.forEach(this.endpoints::add);
        return this;
    }

    public void write() {
        String ngModuleName = name.get();
        Preconditions.checkNotNull(ngModuleName);

        String filename = TemplateUtils.camelCaseToDash(ngModuleName);

        TemplateEvaluator
                .forResource("typescript-ngmodule.ts.vm")
                .variables(context)
                .variable("classes", endpoints)
                .variable("ngModuleName", ngModuleName)
                .write(TypeScriptUtils.fileWriter(filename + ".ts"));
        GeneratedClassTracker.current().addFile(filename);
    }
}
