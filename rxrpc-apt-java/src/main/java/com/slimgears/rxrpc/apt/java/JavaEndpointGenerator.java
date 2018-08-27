/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.EndpointGenerator;

import javax.lang.model.element.ElementKind;

@AutoService(EndpointGenerator.class)
public class JavaEndpointGenerator implements EndpointGenerator {
    @Override
    public void generate(Context context) {
        if (context.hasOption("rxrpc.java.client")) {
            generateClass(context, "_RxClient", "/java-client.java.vm");
        }
        if (context.hasOption("rxrpc.java.server")) {
            generateClass(context, "_RxModule", "/java-server.java.vm");
        }
    }

    private void generateClass(Context context, String classNameSuffix, String templatePath) {
        String className = context.sourceTypeElement().getQualifiedName() + classNameSuffix;
        context.log().info("Generating class: {}", className);

        TypeInfo targetClass = TypeInfo.of(className);
        ImportTracker importTracker = ImportTracker.create(TypeInfo.packageName(className));

        TemplateEvaluator
                .forResource(templatePath)
                .variables(context)
                .variable("isInterface", context.sourceTypeElement().getKind() == ElementKind.INTERFACE)
                .variable("javaUtils", new JavaUtils())
                .variable("autoService", context.hasOption("rxrpc.java.autoservice"))
                .variable("targetClass", targetClass)
                .apply(JavaUtils.imports(importTracker))
                //.postProcess(JavaUtils.formatter())
                .write(JavaUtils.fileWriter(context.environment(), targetClass));
    }
}
