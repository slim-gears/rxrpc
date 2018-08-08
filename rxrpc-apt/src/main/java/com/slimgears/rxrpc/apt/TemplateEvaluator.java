package com.slimgears.rxrpc.apt;

import com.google.auto.value.processor.Utils;
import com.google.escapevelocity.Template;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateEvaluator {
    private final Map<String, Object> templateVariables = new HashMap<>();
    private final Supplier<Reader> reader;

    private TemplateEvaluator(Supplier<Reader> reader) {
        this.reader = reader;
    }

    public static TemplateEvaluator forReader(Supplier<Reader> reader) {
        return new TemplateEvaluator(reader);
    }

    public static TemplateEvaluator forStream(Supplier<InputStream> stream) {
        return forReader(() -> new InputStreamReader(stream.get()));
    }

    public static TemplateEvaluator forResource(String path) {
        return forStream(() -> TemplateEvaluator.class.getResourceAsStream(path));
    }

    public <T> TemplateEvaluator variable(String name, T value) {
        templateVariables.put(name, value);
        return this;
    }

    public <T> TemplateEvaluator variables(T variables) {
        Set<String> objectMethods = Stream.of(Object.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        Class cls = variables.getClass();
        Stream.of(cls.getMethods())
                .filter(m -> m.getParameterCount() == 0 &&
                        !objectMethods.contains(m.getName()) &&
                        Modifier.isPublic(m.getModifiers()) &&
                        !Modifier.isStatic(m.getModifiers()))
                .forEach(m -> {
                    try {
                        m.setAccessible(true);
                        variable(m.getName(), m.invoke(variables));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                    }
                });

        return this;
    }

    public String evaluate() {
        try {
            Template template = Template.parseFrom(reader.get());
            String source = template.evaluate(templateVariables);
            return Utils.reformat(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
