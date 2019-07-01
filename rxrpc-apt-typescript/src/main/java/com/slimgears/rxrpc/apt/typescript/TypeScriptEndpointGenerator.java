/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.auto.service.AutoService;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.EndpointGenerator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.slimgears.util.stream.Streams.ofType;

@AutoService(EndpointGenerator.class)
@SupportedOptions({
        TypeScriptEndpointGenerator.generateNgModuleOption,
        TypeScriptEndpointGenerator.typeMapsOption
})
public class TypeScriptEndpointGenerator implements EndpointGenerator {
    static final String generateNgModuleOption = "rxrpc.ts.ngmodule";
    static final String typeMapsOption = "rxrpc.ts.typemaps";

    private final static Logger log = LoggerFactory.getLogger(TypeScriptEndpointGenerator.class);
    private final Collection<TypeElement> generatedInterfaces = new HashSet<>();

    @Override
    public void generate(Context context) {
        if (!context.meta().generateClient()) {
            return;
        }

        TypeInfo targetClass = TypeInfo.of(context.sourceTypeElement().getSimpleName().toString() + "Client");
        generateCode(
                context,
                targetClass,
                typeScriptUtils -> {
                    if (ElementUtils.isInterface(context.sourceTypeElement())) {
                        ensureInterfaceGenerated((DeclaredType) context.sourceTypeElement().asType(), context);
                        return Collections.singleton(TypeInfo.of(context.sourceClass().simpleName()));
                    } else {
                        return getInterfaces(context, typeScriptUtils);
                    }
                },
                "typescript-client-class.ts.vm");
        GeneratedClassTracker.current().addEndpoint(targetClass);
    }

    private void generateCode(Context context,
                              TypeInfo targetClass,
                              Function<TypeScriptUtils, Collection<TypeInfo>> interfaceProvider,
                              String templateName) {
        ImportTracker importTracker = ImportTracker.create("");
        context.sourceTypeElement()
                .getTypeParameters()
                .stream()
                .map(Element::getSimpleName)
                .map(Name::toString)
                .map(TypeInfo::of)
                .forEach(importTracker::knownClass);

        String filename = TemplateUtils.camelCaseToDash(targetClass.name()) + ".ts";

        log.debug("Generating code for source type: {}", context.sourceTypeElement().getQualifiedName());
        log.debug("Target class name: {}", targetClass.name());

        log.debug("Target file name: {}", filename);

        String ngModuleName = Optional
                .ofNullable(context.moduleName())
                .filter(m -> !m.isEmpty())
                .map(TypeScriptModuleGenerator::toModuleClassName)
                .orElse(null);

        TypeScriptUtils typeScriptUtils = TypeScriptUtils.create();
        TemplateEvaluator.forResource(templateName)
                .variable("targetClass", targetClass)
                .variable("generateNgModule", context.hasOption(generateNgModuleOption))
                .variable("ngModuleName", ngModuleName)
                .variable("tsUtils", typeScriptUtils)
                .variable("interfaces", interfaceProvider.apply(typeScriptUtils))
                .variables(context)
                .apply(typeScriptUtils.imports(importTracker))
                .write(TypeScriptUtils.fileWriter(filename));

        GeneratedClassTracker.current().addClass(
                TypeInfo.of(context.sourceTypeElement()),
                targetClass);
    }

    private Collection<TypeInfo> getInterfaces(Context context, TypeScriptUtils typeScriptUtils) {
        TypeElement typeElement = context.sourceTypeElement();

        Stream<? extends TypeMirror> interfaceTypes = typeElement.getInterfaces().stream();
        return interfaceTypes
                .flatMap(ofType(DeclaredType.class))
                .map(t -> ensureInterfaceGenerated(t, context))
                .map(TypeInfo::of)
                .map(typeScriptUtils::toTypeScriptType)
                .collect(Collectors.toList());
    }

    private void generateInterface(Context context) {
        generateCode(
                context,
                TypeInfo.of(context.sourceTypeElement().getSimpleName().toString()),
                typeScriptUtils -> getInterfaces(context, typeScriptUtils),
                "typescript-client-interface.ts.vm");
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
