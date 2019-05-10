package com.slimgears.rxrpc.apt.java;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.JavaUtils;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.EndpointGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.SupportedOptions;

@SuppressWarnings("WeakerAccess")
@AutoService(EndpointGenerator.class)
public class JavaEndpointGenerator implements EndpointGenerator {
    private final static Logger log = LoggerFactory.getLogger(JavaEndpointGenerator.class);

    static final String rxModuleClassSuffix = "_RxModule";
    static final String rxClientClassSuffix = "_RxClient";

    @Override
    public void generate(Context context) {
        generateClass(context, rxClientClassSuffix, "java-client.java.vm");
        generateClass(context, rxModuleClassSuffix, "java-server.java.vm");
    }

    static TypeInfo rxModuleFromEndpoint(TypeInfo endpointClass) {
        return TypeInfo.of(endpointClass.erasureName() + rxModuleClassSuffix);
    }

    private void generateClass(Context context, String classNameSuffix, String templatePath) {
        String className = context.sourceTypeElement().getQualifiedName() + classNameSuffix;
        context.log().info("Generating class: {}", className);

        TypeInfo targetClass = TypeInfo.of(className);
        ImportTracker importTracker = ImportTracker.create(TypeInfo.packageName(className));

        TemplateEvaluator
                .forResource(templatePath)
                .variables(context)
                .variable("hasModuleName", !Strings.isNullOrEmpty(context.moduleName()))
                .variable("isInterface", ElementUtils.isInterface(context.sourceTypeElement()))
                .variable("javaUtils", new JavaUtils())
                .variable("targetClass", targetClass)
                .apply(JavaUtils.imports(importTracker))
                //.postProcess(JavaUtils.formatter())
                .write(JavaUtils.fileWriter(context.environment(), targetClass));
    }
}
