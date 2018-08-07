/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.ClassInfo;
import com.slimgears.rxrpc.apt.data.EndpointContext;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(ClientGenerator.class)
public class JavaClientGenerator implements ClientGenerator {
    @Override
    public void generateClient(EndpointContext context) {
        String generatedClassName = context.sourceTypeElement().getQualifiedName() + "_RxClient";
        ImportTracker importTracker = ImportTracker.create(ClassInfo.packageName(generatedClassName));
        TypeInfo targetClass = TypeInfo.of(generatedClassName);

        String code = TemplateEvaluator
                .forResource("/JavaClient.java.vm")
                .variables(context)
                .variable("targetClass", targetClass)
                .variable("imports", importTracker)
                .evaluate();

        String importsStr = Stream.of(importTracker.imports())
                .map(name -> "import " + name + ";")
                .collect(Collectors.joining("\n"));

        code = code.replace("`imports`", importsStr);

        context.writeSourceFile(targetClass, code);
    }
}
