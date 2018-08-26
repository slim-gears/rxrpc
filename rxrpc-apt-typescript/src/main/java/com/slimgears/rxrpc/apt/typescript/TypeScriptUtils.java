/**
 *
 */
package com.slimgears.rxrpc.apt.typescript;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Inject;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.ImportTracker;
import com.slimgears.rxrpc.apt.util.LogUtils;
import com.slimgears.rxrpc.apt.util.TemplateEvaluator;
import com.slimgears.rxrpc.apt.util.TemplateUtils;
import com.slimgears.rxrpc.apt.util.TypeConverter;
import com.slimgears.rxrpc.apt.util.TypeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Named;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeScriptUtils extends TemplateUtils {

    private final static Logger log = LoggerFactory.getLogger(TypeScriptUtils.class);
    private final AtomicReference<TypeConverter> configuredTypeConverter = new AtomicReference<>(TypeConverters.empty);
    private final static Multimap<TypeInfo, TypeInfo> generatedClasses = TreeMultimap.create(TypeInfo.comparator, TypeInfo.comparator);
    private final static Set<TypeInfo> generatedEndpoints = new TreeSet<>(TypeInfo.comparator);
    private final AtomicReference<TypeConverter> typeConverter = new AtomicReference<>(TypeConverters.empty);
    private final TypeConverter fallbackTypeConverter = TypeConverters.create(t -> true, (up, t) -> convertRecursively(t));

    @Inject
    public TypeScriptUtils() {
        addTypeConverter(TypeConverters.fromPropertiesResource("/types.properties"));
    }

    @Inject(optional = true)
    private void addTypeMap(@Named("rxrpc.ts.typemap") Path path) {
        addTypeConverter(TypeConverters.fromPropertiesFile(path));
    }

    private void addTypeConverter(TypeConverter typeConverter) {
        this.configuredTypeConverter.updateAndGet(old -> old.combineWith(typeConverter));
        this.typeConverter.set(this.configuredTypeConverter.get().combineWith(fallbackTypeConverter));
    }

    public void addGeneratedClass(TypeInfo source, TypeInfo generated) {
        generatedClasses.put(source, generated);
    }

    public void addGeneratedEndpoint(TypeInfo generated) {
        generatedEndpoints.add(generated);
    }

    public ImmutableMultimap<TypeInfo, TypeInfo> getGeneratedClasses() {
        return ImmutableMultimap.copyOf(generatedClasses);
    }

    public ImmutableSet<TypeInfo> getGeneratedEndpoints() {
        return ImmutableSet.copyOf(generatedEndpoints);
    }

    public boolean isSupportedType(Class cls) {
        return isSupportedType(TypeInfo.of(cls));
    }

    public boolean isSupportedType(TypeInfo type) {
        return typeConverter.get().canConvert(type);
    }

    public TypeInfo toTypeScriptType(TypeInfo type) {
        return typeConverter.get().convert(type);
    }

    public Consumer<String> fileWriter(ProcessingEnvironment environment, String filename) {
        return content -> writeFile(environment, filename, content.trim() + "\n");
    }

    public void writeIndex(ProcessingEnvironment environment) {
        writeFile(environment, "index.ts", generateIndex());
    }

    public Function<TemplateEvaluator, TemplateEvaluator> imports(ImportTracker importTracker) {
        return evaluator -> evaluator
                .variable("imports", importTracker)
                .postProcess(TemplateUtils.postProcessImports(importTracker))
                .postProcess(code -> addImports(importTracker, code));
    }

    private String addImports(ImportTracker importTracker, String code) {
        String importsStr = Stream.of(importTracker.usedClasses())
                .filter(type -> !configuredTypeConverter.get().canConvert(type))
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

    public String generateIndex() {
        return getGeneratedClasses()
                .values()
                .stream()
                .map(type -> "export * from './" + TemplateUtils.camelCaseToDash(type.simpleName()) + "';")
                .collect(Collectors.joining("\n"));
    }

    private TypeInfo convertRecursively(TypeInfo typeInfo) {
        if (typeInfo.typeParams().isEmpty()) {
            return TypeInfo.of(typeInfo.simpleName());
        }

        TypeInfo.Builder builder = TypeInfo.builder().name(typeInfo.simpleName());
        typeInfo.typeParams().forEach(tp -> builder.typeParam(tp.name(), typeConverter.get().convert(tp.type())));
        return builder.build();
    }
}
