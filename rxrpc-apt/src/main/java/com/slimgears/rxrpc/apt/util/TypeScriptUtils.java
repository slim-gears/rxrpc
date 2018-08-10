/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.slimgears.rxrpc.apt.data.TypeConverter;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.data.TypeParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeScriptUtils extends TemplateUtils {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptUtils.class);
    public final static String typeScriptOutputDirOption = "tsOutDir";

    private final static ImmutableMap<TypeInfo, TypeInfo> typeMapping = ImmutableMap.<TypeInfo, TypeInfo>builder()
            .putAll(types("number",
                    byte.class, Byte.class,
                    short.class, Short.class,
                    int.class, Integer.class,
                    long.class, Long.class,
                    float.class, Float.class,
                    double.class, Double.class,
                    BigInteger.class,
                    BigDecimal.class))
            .putAll(types("string", String.class, char.class, Character.class, CharSequence.class))
            .putAll(types("boolean", boolean.class, Boolean.class))
            .putAll(types("any", JsonNode.class, Object.class))
            .build();

    private static Map<TypeInfo, TypeInfo> types(String toType, Class... cls) {
        return Stream.of(cls)
                .map(Class::getName)
                .map(TypeInfo::of)
                .collect(Collectors.toMap(t -> t, t -> TypeInfo.of(toType)));
    }

    private final static TypeConverter typeConverter = TypeConverter.ofMultiple(
            TypeConverter.create(typeMapping::containsKey, typeMapping::get),
            TypeConverter.create(type -> type.is(Map.class), type -> convertTypeParams(type, "Map")),
            TypeConverter.create(type -> type.is(List.class), type -> TypeInfo.arrayOf(convertType(type.elementType()))),
            TypeConverter.create(TypeInfo::isArray, TypeScriptUtils::convertArray),
            TypeConverter.create(type -> true, type -> TypeInfo.of(type.simpleName())));

    private final ImportTracker importTracker;

    public TypeScriptUtils(ImportTracker importTracker) {
        this.importTracker = importTracker;
    }

    public boolean isSupportedType(Class cls) {
        return isSupportedType(TypeInfo.of(cls));
    }

    public boolean isSupportedType(TypeInfo type) {
        return typeMapping.containsKey(type);
    }

    public TypeInfo toTypeScriptType(TypeInfo type) {
        return typeConverter.convert(type);
    }

    public static Consumer<String> fileWriter(ProcessingEnvironment environment, String filename) {
        return content -> writeFile(environment, filename, content);
    }

    public static Function<TemplateEvaluator, TemplateEvaluator> imports(ImportTracker importTracker) {
        return evaluator -> evaluator
                .variable("imports", importTracker)
                .postProcess(applyTypeScriptImports(importTracker));
    }

    private static Function<String, String> applyTypeScriptImports(ImportTracker importTracker) {
        return code -> code;
    }

    private static void writeFile(ProcessingEnvironment environment, String filename, String content) {
        log.info("Writing file: {}", filename);
        log.info(content);

        Filer filer = environment.getFiler();
        FileObject fileObject = Optional
                .ofNullable(environment.getOptions().get("tsOutDir"))
                .map(dir -> Paths.get(dir, filename))
                .map(Safe.of(path -> filer.createResource(StandardLocation.SOURCE_OUTPUT, path.toString(), filename)))
                .orElseGet(Safe.of(() -> filer.createResource(StandardLocation.SOURCE_OUTPUT, "typescript", filename)));
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

    private static TypeInfo convertType(TypeInfo type) {
        return typeConverter.convert(type);
    }

    private static TypeInfo convertArray(TypeInfo arrayType) {
        Preconditions.checkArgument(arrayType.isArray());
        return TypeInfo.of(convertType(arrayType.elementType()).name() + "[]");
    }

    private static TypeInfo convertTypeParams(TypeInfo typeInfo, String newName) {
        return TypeInfo.builder()
                .name(newName)
                .typeParams(typeInfo.typeParams()
                        .stream()
                        .map(TypeParameterInfo::type)
                        .map(TypeScriptUtils::convertType)
                        .toArray(TypeInfo[]::new))
                .build();
    }
}
