/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.ImportTracker;
import com.slimgears.rxrpc.apt.util.JavaUtils;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;

import javax.lang.model.element.ElementKind;

@AutoService(EndpointGenerator.class)
public class JavaEndpointGenerator implements EndpointGenerator {
    @Override
    public void generate(Context context) {
        generateClass(context, "_RxClient", "/java-client.java.vm");
        generateClass(context, "_RxModule", "/java-server.java.vm");
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
                .variable("targetClass", targetClass)
                .apply(JavaUtils.imports(importTracker))
                //.postProcess(JavaUtils.formatter())
                .write(JavaUtils.fileWriter(context.environment(), targetClass));
    }
}
