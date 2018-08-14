/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.slimgears.rxrpc.apt.data.TypeConverter;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.data.TypeParameterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Generated;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
            .putAll(types("Map", Map.class))
            .build();

    private static Map<TypeInfo, TypeInfo> types(String toType, Class... cls) {
        return Stream.of(cls)
                .map(Class::getName)
                .map(TypeInfo::of)
                .collect(Collectors.toMap(t -> t, t -> TypeInfo.of(toType)));
    }

    private final static Map<TypeInfo, TypeInfo> generatedClasses = new TreeMap<>(TypeInfo.comparator);
    private final static Set<TypeInfo> generatedEndpoints = new TreeSet<>(TypeInfo.comparator);

    private final TypeConverter typeConverter = TypeConverter.ofMultiple(
            TypeConverter.create(typeMapping::containsKey, typeMapping::get),
            TypeConverter.create(type -> type.is(Map.class), type -> convertTypeParams(type, "Map")),
            TypeConverter.create(type -> type.is(List.class), type -> TypeInfo.arrayOf(convertType(type.elementType()))),
            TypeConverter.create(TypeInfo::isArray, this::convertArray),
            TypeConverter.create(generatedClasses::containsKey, generatedClasses::get),
            TypeConverter.create(type -> true, type -> TypeInfo.of(type.simpleName())));

    private final ImportTracker importTracker;

    public static void addGeneratedClass(TypeInfo source, TypeInfo generated) {
        generatedClasses.put(source, generated);
    }

    public static void addGeneratedEndpoint(TypeInfo generated) {
        generatedEndpoints.add(generated);
    }

    public static ImmutableMap<TypeInfo, TypeInfo> getGeneratedClasses() {
        return ImmutableMap.copyOf(generatedClasses);
    }

    public static ImmutableSet<TypeInfo> getGeneratedEndpoints() {
        return ImmutableSet.copyOf(generatedEndpoints);
    }

    public TypeScriptUtils(ImportTracker importTracker) {
        this.importTracker = importTracker;
    }

    public ImportTracker imports() {
        return importTracker;
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
        return content -> {
            writeFile(environment, filename, content.trim() + "\n");
        };
    }

    public static void writeIndex(ProcessingEnvironment environment) {
        writeFile(environment, "index.ts", generateIndex());
    }

    public static Function<TemplateEvaluator, TemplateEvaluator> imports(ImportTracker importTracker) {
        return evaluator -> evaluator
                .variable("imports", importTracker)
                .postProcess(TemplateUtils.postProcessImports(importTracker))
                .postProcess(code -> addImports(importTracker, code));
    }

    private static String addImports(ImportTracker importTracker, String code) {
        String importsStr = Stream.of(importTracker.usedClasses())
                .filter(type -> !typeMapping.containsValue(type))
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
                            .orElse("./");

                    if (packagePath.startsWith("/")) {
                        packagePath = "." + packagePath;
                    }

                    return entry.getValue()
                            .stream()
                            .sorted()
                            .collect(Collectors.joining(
                                    ", ",
                                    "import { ",
                                    " } from '" + packagePath + "';"));
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

    private TypeInfo convertType(TypeInfo type) {
        return typeConverter.convert(type);
    }

    private TypeInfo convertArray(TypeInfo arrayType) {
        Preconditions.checkArgument(arrayType.isArray());
        return TypeInfo.of(convertType(arrayType.elementType()).name() + "[]");
    }

    private TypeInfo convertTypeParams(TypeInfo typeInfo, String newName) {
        return TypeInfo.builder()
                .name(newName)
                .typeParams(typeInfo.typeParams()
                        .stream()
                        .map(TypeParameterInfo::type)
                        .map(this::convertType)
                        .toArray(TypeInfo[]::new))
                .build();
    }

    public static String generateIndex() {
        return getGeneratedClasses()
                .values()
                .stream()
                .map(type -> "export * from './" + TemplateUtils.camelCaseToDash(type.simpleName()) + "';")
                .collect(Collectors.joining("\n"));
    }
}
