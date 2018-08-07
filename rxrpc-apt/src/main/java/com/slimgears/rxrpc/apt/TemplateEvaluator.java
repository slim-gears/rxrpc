package com.slimgears.rxrpc.apt;

import com.google.auto.value.processor.Utils;
import com.google.escapevelocity.Template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    public String evaluate() {
        try {
            Template template = Template.parseFrom(reader.get());
            return Utils.reformat(template.evaluate(templateVariables));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
