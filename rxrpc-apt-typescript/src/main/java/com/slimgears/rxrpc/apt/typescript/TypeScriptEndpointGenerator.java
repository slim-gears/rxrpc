/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.rxrpc.apt.EndpointGenerator;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(EndpointGenerator.class)
public class TypeScriptEndpointGenerator implements EndpointGenerator {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptEndpointGenerator.class);
    private final Collection<TypeElement> generatedInterfaces = new HashSet<>();

    @Override
    public void generate(Context context) {
        TypeInfo targetClass = TypeInfo.of(context.sourceTypeElement().getSimpleName().toString() + "Client");
        Collection<TypeInfo> interfaces = getInterfaces(context);
        if (ElementUtils.isInterface(context.sourceTypeElement())) {
            ensureInterfaceGenerated((DeclaredType) context.sourceTypeElement().asType(), context);
            interfaces = Collections.singleton(TypeInfo.of(context.sourceClass().simpleName()));
        }
        generateCode(
                context,
                targetClass,
                interfaces,
                "/typescript-client-class.ts.vm");
        TypeScriptUtils.addGeneratedEndpoint(targetClass);
    }

    private void generateCode(Context context, TypeInfo targetClass, Collection<TypeInfo> interfaces, String templateName) {
        ImportTracker importTracker = ImportTracker.create("");
        String filename = TemplateUtils.camelCaseToDash(targetClass.name()) + ".ts";

        log.debug("Generating code for source type: {}", context.sourceTypeElement().getQualifiedName());
        log.debug("Target class name: {}", targetClass.name());

        log.debug("Target file name: {}", filename);

        TemplateEvaluator.forResource(templateName)
                .variable("targetClass", targetClass)
                .variable("generateNgModule", context.hasOption("rxrpc.ts.ngmodule"))
                .variable("tsUtils", new TypeScriptUtils(importTracker))
                .variable("interfaces", interfaces)
                .variables(context)
                .apply(TypeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(context.environment(), filename));

        TypeScriptUtils.addGeneratedClass(
                TypeInfo.of(context.sourceTypeElement()),
                targetClass);
    }

    private Collection<TypeInfo> getInterfaces(Context context) {
        TypeElement typeElement = context.sourceTypeElement();

        Stream<? extends TypeMirror> interfaceTypes = typeElement.getInterfaces().stream();
        return StreamUtils.ofType(DeclaredType.class, interfaceTypes)
                .map(t -> ensureInterfaceGenerated(t, context))
                .map(DeclaredType::asElement)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .map(TypeElement::getSimpleName)
                .map(Object::toString)
                .map(TypeInfo::of)
                .collect(Collectors.toList());
    }

    private void generateInterface(Context context) {
        generateCode(
                context,
                TypeInfo.of(context.sourceTypeElement().getSimpleName().toString()),
                getInterfaces(context),
                "/typescript-client-interface.ts.vm");
    }

    private DeclaredType ensureInterfaceGenerated(DeclaredType typeMirror, Context context) {
        TypeElement ifaceElement = (TypeElement)typeMirror.asElement();
        if (ifaceElement.getKind() != ElementKind.INTERFACE || generatedInterfaces.contains(ifaceElement)) {
            return typeMirror;
        }

        generatedInterfaces.add(ifaceElement);
        context = context
                .toBuilder()
                .sourceTypeElement(ifaceElement)
                .build();

        generateInterface(context);
        return typeMirror;
    }
}
