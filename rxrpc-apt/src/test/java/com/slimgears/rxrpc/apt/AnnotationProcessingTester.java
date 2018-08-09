/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.common.collect.Iterables;
import com.google.testing.compile.JavaFileObjects;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AnnotationProcessingTester {
    private final Collection<JavaFileObject> inputFiles = new ArrayList<>();
    private final Collection<JavaFileObject> expectedFiles = new ArrayList<>();
    private final Collection<AbstractProcessor> processors = new ArrayList<>();
    private final Collection<String> options = new ArrayList<>();

    public static AnnotationProcessingTester create() {
        return new AnnotationProcessingTester();
    }

    public AnnotationProcessingTester options(String... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    public AnnotationProcessingTester inputFiles(String... files) {
        fromResources("input", files).forEach(inputFiles::add);
        return this;
    }

    public AnnotationProcessingTester expectedFiles(String... files) {
        fromResources("output", files).forEach(expectedFiles::add);
        return this;
    }

    public AnnotationProcessingTester processedWith(AbstractProcessor... processors) {
        this.processors.addAll(Arrays.asList(processors));
        return this;
    }

    public void test() {
        assert_()
                .about(javaSources())
                .that(inputFiles)
                .withCompilerOptions(options)
                .processedWith(processors)
                .compilesWithoutError()
                .and().generatesSources(Iterables.getFirst(expectedFiles, null), Stream.of(expectedFiles).skip(1).toArray(JavaFileObject[]::new));
    }

    private static Iterable<JavaFileObject> fromResources(final String path, String[] files) {
        return Arrays.stream(files)
                .map(input -> JavaFileObjects.forResource(path + '/' + input))
                .collect(Collectors.toList());
    }
}
