/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import org.apache.commons.io.IOUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AnnotationProcessingTester {
    private final Collection<JavaFileObject> inputFiles = new ArrayList<>();
    private final Collection<AbstractProcessor> processors = new ArrayList<>();
    private final Collection<String> options = new ArrayList<>();
    private final Collection<Function<CompileTester.SuccessfulCompilationClause, CompileTester.SuccessfulCompilationClause>> assertions = new ArrayList<>();

    public static AnnotationProcessingTester create() {
        return new AnnotationProcessingTester().options("-Averbosity=DEBUG");
    }

    public AnnotationProcessingTester options(String... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    public AnnotationProcessingTester inputFiles(String... files) {
        inputFiles.addAll(fromResources("input", files));
        return this;
    }

    public AnnotationProcessingTester expectedSources(String... files) {
        List<JavaFileObject> sources = fromResources("output", files);
        assertions.add(s -> s.and().generatesSources(sources.get(0), sources.stream().skip(1).toArray(JavaFileObject[]::new)));
        return this;
    }

    public AnnotationProcessingTester expectedFiles(String... files) {
        return expectedFiles(fromResources("output", files));
    }

    public AnnotationProcessingTester expectedFile(String name, String... lines) {
        return expectedFiles(JavaFileObjects.forSourceLines(name, lines));
    }

    public AnnotationProcessingTester processedWith(AbstractProcessor... processors) {
        this.processors.addAll(Arrays.asList(processors));
        return this;
    }

    public void test() {
        CompileTester.SuccessfulCompilationClause compilationClause = assert_()
                .about(javaSources())
                .that(inputFiles)
                .withCompilerOptions(options)
                .processedWith(processors)
                .compilesWithoutError();

        assertions.stream()
                .reduce(Function::andThen)
                .orElse(c -> c)
                .apply(compilationClause);
    }

    private AnnotationProcessingTester expectedFiles(JavaFileObject... files) {
        return expectedFiles(Arrays.asList(files));
    }

    private AnnotationProcessingTester expectedFiles(List<JavaFileObject> files) {
        assertions.add(s -> s.and().generatesFiles(files.get(0), files.stream().skip(1).toArray(JavaFileObject[]::new)));
        return this;
    }

    private static List<JavaFileObject> fromResources(final String path, String[] files) {
        return Arrays.stream(files)
                .map(input -> JavaFileObjects.forResource(path + '/' + input))
                .collect(Collectors.toList());
    }

    private static JavaFileObject forResource(String filename, JavaFileObject.Kind kind) {
        return new ResourceJavaFileObject(filename, kind);
    }

    private static class ResourceJavaFileObject extends SimpleJavaFileObject {
        private final String content;

        ResourceJavaFileObject(String resourceName, Kind kind) {
            super(URI.create(Resources.getResource(resourceName).toString()), kind);
            try {
                content = Resources.toString(toUri().toURL(), Charsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream openInputStream() {
            return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        }
    }
}
