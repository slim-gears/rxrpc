/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.apt.data.EnumInfo;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.DataClassGenerator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Collection<TypeInfo> interfaces = Stream.concat(
                Stream.of(context.sourceTypeElement().getSuperclass()),
                context.sourceTypeElement().getInterfaces().stream())
                .flatMap(ElementUtils::toTypeElement)
                .filter(ElementUtils::isUnknownType)
                .map(TypeInfo::of)
                .map(typeScriptUtils::toTypeScriptType)
                .collect(Collectors.toList());

        evaluator(context)
                .variable("targetClass", targetClass)
                .variable("tsUtils", typeScriptUtils)
                .variable("interfaces", interfaces)
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
