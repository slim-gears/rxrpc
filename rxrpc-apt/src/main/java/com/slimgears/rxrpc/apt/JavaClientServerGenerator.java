/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.ClassInfo;
import com.slimgears.rxrpc.apt.data.EndpointContext;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(EndpointGenerator.class)
public class JavaClientServerGenerator implements EndpointGenerator {
    @Override
    public void generate(EndpointContext context) {
        generateClass(context, "_RxClient", "/java-client.java.vm");
        generateClass(context, "_RxModule", "/java-server.java.vm");
    }

    private void generateClass(EndpointContext context, String classNameSuffix, String templatePath) {

        String className = context.sourceTypeElement().getQualifiedName() + classNameSuffix;
        System.out.println("Generating class: " + className);

        TypeInfo targetClass = TypeInfo.of(className);
        ImportTracker importTracker = ImportTracker.create(ClassInfo.packageName(className));

        String code = TemplateEvaluator
                .forResource(templatePath)
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
