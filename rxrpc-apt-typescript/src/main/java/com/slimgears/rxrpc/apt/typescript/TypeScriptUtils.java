/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.*;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeScriptUtils extends TemplateUtils {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptUtils.class);

    private final TypeConverter configuredTypeConverter = TypeConverters.ofMultiple(
            TypeConverters.fromPropertiesResource("/types.properties"),
            TypeConverters.fromEnvironmentMaps("rxrpc.ts.typemaps"));
    private final TypeConverter typeConverter;

    private TypeScriptUtils() {
        this.typeConverter =TypeConverters.ofMultiple(
                configuredTypeConverter,
                TypeConverters.create(type -> true, TypeScriptUtils::convertRecursively));
    }

    public static TypeScriptUtils create() {
        return new TypeScriptUtils();
    }

    public boolean isSupportedType(Class cls) {
        return isSupportedType(TypeInfo.of(cls));
    }

    public boolean isSupportedType(TypeInfo type) {
        return configuredTypeConverter.canConvert(type);
    }

    public TypeInfo toTypeScriptType(TypeInfo type) {
        TypeInfo tsType = typeConverter.convert(type);
        log.trace("Type conversion {} -> {}", type, tsType);
        return tsType;
    }

    public static Consumer<String> fileWriter(String filename) {
        String directory = Optional
                .ofNullable(Environment.instance().processingEnvironment().getOptions().get("tsOutDir"))
                .map(dir -> Paths.get(dir, filename))
                .map(Path::toString)
                .orElse("typescript");

        return FileUtils.fileWriter(directory + "/" + filename);
    }

    public Function<TemplateEvaluator, TemplateEvaluator> imports(ImportTracker importTracker) {
        return evaluator -> evaluator
                .variable("imports", importTracker)
                .postProcess(TemplateUtils.postProcessImports(importTracker, name -> name.replaceFirst("^@", "___at___")))
                .postProcess(code -> addImports(importTracker, code));
    }

    private String addImports(ImportTracker importTracker, String code) {
        String importsStr = Stream.of(importTracker.usedClasses())
                .filter(type -> !configuredTypeConverter.canConvert(type))
                .collect(Collectors.groupingBy(
                        TypeInfo::packageName,
                        TreeMap::new,
                        Collectors.mapping(TypeInfo::simpleName, Collectors.toList())))
                .entrySet()
                .stream()
                .map(entry -> {
                    String packagePath = Optional
                            .of(camelCaseToDash(entry.getKey()).replace('.', '/'))
                            .filter(str -> !str.isEmpty())
                            .orElse("./index");

                    if (packagePath.startsWith("/")) {
                        packagePath = "." + packagePath;
                    }

                    return entry.getValue()
                            .stream()
                            .sorted()
                            .collect(Collectors.joining(
                                    ", ",
                                    "import { ",
                                    " } from '" + packagePath.replace("___at___", "@") + "';"));
                })
                .collect(Collectors.joining("\n"));
        return code.replace(importTracker.toString(), importsStr);
    }


    private static TypeInfo convertRecursively(TypeConverter typeConverter, TypeInfo typeInfo) {
        if (typeInfo.hasEnclosingType()) {
            typeInfo = typeInfo
                    .toBuilder()
                    .name(typeInfo.name().replace("$", ""))
                    .build();
        }

        if (typeInfo.typeParams().isEmpty()) {
            return TypeInfo.of(toSimpleName(typeInfo));
        }

        if (typeInfo.isArray()) {
            return TypeInfo.arrayOf(convertRecursively(typeConverter, typeInfo.elementTypeOrSelf()));
        }

        TypeInfo.Builder builder = TypeInfo.builder().name(toSimpleName(typeInfo));
        typeInfo.typeParams().forEach(tp -> builder.typeParam(tp.name(), typeConverter.convert(tp.type())));
        return builder.build();
    }

    private static String toSimpleName(TypeInfo typeInfo) {
        return GeneratedClassTracker
                .current()
                .resolveGeneratedClass(typeInfo)
                .simpleName();
    }
}
