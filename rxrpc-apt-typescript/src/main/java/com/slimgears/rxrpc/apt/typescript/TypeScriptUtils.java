/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeScriptUtils extends TemplateUtils {
    private final static Logger log = LoggerFactory.getLogger(TypeScriptUtils.class);

    private final static Multimap<TypeInfo, TypeInfo> generatedClasses = TreeMultimap.create(TypeInfo.comparator, TypeInfo.comparator);
    private final static Set<TypeInfo> generatedEndpoints = new TreeSet<>(TypeInfo.comparator);
    private final TypeConverter configuredTypeConverter = TypeConverters.ofMultiple(
            TypeConverters.fromPropertiesResource("/types.properties"),
            TypeConverters.fromEnvironmentMaps("rxrpc.ts.typemaps"));
    private final TypeConverter typeConverter;

    public TypeScriptUtils(ImportTracker importTracker) {
        this.typeConverter = toImportTrackedConverter(TypeConverters.ofMultiple(
                configuredTypeConverter,
                TypeConverters.create(type -> true, TypeScriptUtils::convertRecursively)), importTracker);
    }

    public static void addGeneratedClass(TypeInfo source, TypeInfo generated) {
        generatedClasses.put(source, generated);
    }

    public static void addGeneratedEndpoint(TypeInfo generated) {
        generatedEndpoints.add(generated);
    }

    public static ImmutableMultimap<TypeInfo, TypeInfo> getGeneratedClasses() {
        return ImmutableMultimap.copyOf(generatedClasses);
    }

    public static ImmutableSet<TypeInfo> getGeneratedEndpoints() {
        return ImmutableSet.copyOf(generatedEndpoints);
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

    public static void writeIndex(ProcessingEnvironment environment) {
        writeFile(environment, "index.ts", generateIndex());
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

    public static String generateIndex() {
        return getGeneratedClasses()
                .values()
                .stream()
                .map(type -> "export * from './" + TemplateUtils.camelCaseToDash(type.simpleName()) + "';")
                .collect(Collectors.joining("\n"));
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
        return Optional
                .ofNullable(generatedClasses.get(typeInfo))
                .map(c -> Iterables.getFirst(c, typeInfo))
                .orElse(typeInfo)
                .simpleName();
    }

    private static TypeConverter toImportTrackedConverter(TypeConverter converter, ImportTracker importTracker) {
        return new TypeConverter() {
            @Override
            public boolean canConvert(TypeInfo typeInfo) {
                return converter.canConvert(typeInfo);
            }

            @Override
            public TypeInfo convert(TypeConverter upstream, TypeInfo typeInfo) {
                TypeInfo converted = converter.convert(this, typeInfo);
                if (!converted.name().startsWith("{")) {
                    importTracker.use(converted);
                }
                return converted;
            }
        };
    }
}
