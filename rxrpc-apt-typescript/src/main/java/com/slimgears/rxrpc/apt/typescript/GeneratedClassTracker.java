package com.slimgears.rxrpc.apt.typescript;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.TemplateUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratedClassTracker {
    private final static ThreadLocal<GeneratedClassTracker> instance = new ThreadLocal<>();
    private final Set<String> generatedFiles = new TreeSet<>(Comparator.naturalOrder());
    private final Set<TypeInfo> generatedClasses = new TreeSet<>(Comparator.comparing(TypeInfo::name));
    private final Multimap<TypeInfo, TypeInfo> generatedClassesMap = TreeMultimap.create(TypeInfo.comparator, TypeInfo.comparator);
    private final Set<TypeInfo> generatedEndpoints = new TreeSet<>(Comparator.comparing(TypeInfo::name));

    public interface Disposable extends AutoCloseable {
        void close();
    }

    private GeneratedClassTracker() {

    }

    public static GeneratedClassTracker current() {
        return Optional
                .ofNullable(instance.get())
                .orElseGet(() -> {
                    GeneratedClassTracker obj = new GeneratedClassTracker();
                    instance.set(obj);
                    return obj;
                });
    }

    public static Disposable trackFiles() {
        GeneratedClassTracker prev = instance.get();
        instance.set(new GeneratedClassTracker());
        return () -> instance.set(prev);
    }

    public void addFile(String filename) {
        generatedFiles.add(filename);
    }

    public void addClass(TypeInfo clazz) {
        generatedClasses.add(clazz);
        addFile(TemplateUtils.camelCaseToDash(clazz.simpleName()));
    }

    public void addClass(TypeInfo source, TypeInfo generated) {
        generatedClassesMap.put(source, generated);
        addClass(generated);
    }

    public void addEndpoint(TypeInfo endpoint) {
        generatedEndpoints.add(endpoint);
        addClass(endpoint);
    }

    public ImmutableList<TypeInfo> generatedEndpoints() {
        return ImmutableList.copyOf(generatedEndpoints);
    }

    public ImmutableList<TypeInfo> generatedClasses() {
        return ImmutableList.copyOf(generatedClasses);
    }

    public ImmutableList<String> generatedFiles() {
        return ImmutableList.copyOf(generatedFiles);
    }

    public TypeInfo resolveGeneratedClass(TypeInfo source) {
        return Optional
                .ofNullable(generatedClassesMap.get(source))
                .map(c -> Iterables.getFirst(c, source))
                .orElse(source);
    }

    public TypeInfo resolveGeneratedEndpoint(TypeInfo source) {
        return Optional
                .ofNullable(generatedClassesMap.get(source))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(generatedEndpoints::contains)
                .findFirst()
                .orElse(source);
    }

    public String generateIndex() {
        return generatedFiles
                .stream()
                .map(file -> "export * from './" + file + "';")
                .collect(Collectors.joining("\n"));
    }
}
