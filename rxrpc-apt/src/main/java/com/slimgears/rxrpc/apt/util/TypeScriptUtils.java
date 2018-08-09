/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.google.common.collect.ImmutableMap;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeScriptUtils extends TemplateUtils {
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
            .build();

    private static final Map<TypeInfo, TypeInfo> types(String toType, Class... cls) {
        return Stream.of(cls)
                .map(Class::getName)
                .map(TypeInfo::of)
                .collect(Collectors.toMap(t -> t, t -> TypeInfo.of(toType)));
    }

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
        return (isSupportedType(type))
                ? typeMapping.get(type)
                : TypeInfo.of(importTracker.use(type));
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
        System.out.println("Writing file: " + filename);
        System.out.println(content);

        Filer filer = environment.getFiler();
        FileObject fileObject = Optional
                .ofNullable(environment.getOptions().get("tsOutDir"))
                .map(dir -> Paths.get(dir, filename))
                .map(Safe.of(path -> filer.createResource(StandardLocation.SOURCE_OUTPUT, "", path.toString())))
                .orElseGet(Safe.of(() -> filer.createResource(StandardLocation.SOURCE_OUTPUT, "", filename)));
        try (Writer writer = fileObject.openWriter()){
            writer.write(content);
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
