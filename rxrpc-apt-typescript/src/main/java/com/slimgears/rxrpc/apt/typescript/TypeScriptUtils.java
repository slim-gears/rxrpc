/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ImportTracker;
import com.slimgears.apt.util.LogUtils;
import com.slimgears.apt.util.TemplateEvaluator;
import com.slimgears.apt.util.TypeConverter;
import com.slimgears.apt.util.TypeConverters;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.util.stream.Safe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
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
        return typeConverter.convert(type);
    }

    public static Consumer<String> fileWriter(ProcessingEnvironment environment, String filename) {
        return content -> {
            writeFile(environment, filename, content.trim() + "\n");
        };
    }

    public Function<TemplateEvaluator, TemplateEvaluator> imports(ImportTracker importTracker) {
        return evaluator -> evaluator
                .variable("imports", importTracker)
                .postProcess(TemplateUtils.postProcessImports(importTracker, name -> name.replace('@', '$')))
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
                                    " } from '" + packagePath.replace('$', '@') + "';"));
                })
                .collect(Collectors.joining("\n"));
        return code.replace(importTracker.toString(), importsStr);
    }

    public static void writeFile(ProcessingEnvironment environment, String filename, String content) {
        log.info("Writing file: {}", filename);
        LogUtils.dumpContent(content);

        Filer filer = environment.getFiler();
        FileObject fileObject = Optional
                .ofNullable(environment.getOptions().get("tsOutDir"))
                .map(dir -> Paths.get(dir, filename))
                .map(Safe.ofFunction(path -> filer.createResource(StandardLocation.SOURCE_OUTPUT, path.toString(), filename)))
                .orElseGet(Safe.ofSupplier(() -> filer.createResource(StandardLocation.SOURCE_OUTPUT, "typescript", filename)));
        try (Writer writer = fileObject.openWriter();
             BufferedWriter bufWriter = new BufferedWriter(writer)) {
            for (String line: content.split("\n")) {
                bufWriter.write(line);
                bufWriter.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static TypeInfo convertRecursively(TypeConverter typeConverter, TypeInfo typeInfo) {
        if (typeInfo.typeParams().isEmpty()) {
            return TypeInfo.of(toSimpleName(typeInfo));
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
