/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.DataClassGenerator;
import com.slimgears.rxrpc.apt.data.EnumInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.ImportTracker;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

@AutoService(DataClassGenerator.class)
public class TypeScriptDataClassGenerator implements DataClassGenerator {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptDataClassGenerator.class);

    @Override
    public void generate(Context context) {
        String className = getTargetTypeName(context.sourceTypeElement());
        ImportTracker importTracker = ImportTracker.create();
        context.sourceTypeElement()
                .getTypeParameters()
                .stream()
                .map(Element::getSimpleName)
                .map(Name::toString)
                .map(TypeInfo::of)
                .forEach(importTracker::knownClass);

        log.debug("Generating code for source type: {}", context.sourceTypeElement().getQualifiedName());
        log.debug("Target class name: {}", className);
        TypeInfo targetClass = TypeInfo.of(className);

        String filename = TemplateUtils.camelCaseToDash(targetClass.name()) + ".ts";
        log.debug("Target file name: {}", filename);

        TypeScriptUtils.addGeneratedClass(
                TypeInfo.of(context.sourceTypeElement()),
                targetClass);

        TypeScriptUtils typeScriptUtils = new TypeScriptUtils();
        evaluator(context)
                .variable("targetClass", targetClass)
                .variable("tsUtils", typeScriptUtils)
                .variables(context)
                .apply(typeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(context.environment(), filename));
    }

    private String getTargetTypeName(Element element) {
        String name = "";
        while (element instanceof TypeElement) {
            name = element.getSimpleName().toString() + name;
            element = element.getEnclosingElement();
        }
        return name;
    }

    private TemplateEvaluator evaluator(Context context) {
        if (ElementUtils.isEnum(context.sourceTypeElement())) {
            return TemplateEvaluator.forResource("/typescript-enum.ts.vm")
                    .variable("enum", EnumInfo.of(context.sourceTypeElement()));
        } else {
            return TemplateEvaluator.forResource("/typescript-data-class.ts.vm");
        }
    }
}
