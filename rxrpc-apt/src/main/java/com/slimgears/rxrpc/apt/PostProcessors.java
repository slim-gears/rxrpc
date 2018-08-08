/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import org.apache.commons.text.StringSubstitutor;

import java.util.function.Function;

public class PostProcessors {
    public static Function<String, String> applyJavaImports(ImportTracker importTracker) {
        return applyImports(importTracker, imp -> "import " + imp + ";");
    }

    public static Function<String, String> formatter() {
        return code -> {
            try {
                return new Formatter(JavaFormatterOptions
                        .builder()
                        .style(JavaFormatterOptions.Style.AOSP)
                        .build())
                        .formatSource(code);
            } catch (FormatterException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static Function<String, String> applyImports(ImportTracker importTracker, Function<String, String> importFormatter) {
        StringSubstitutor classSubstitutor = new StringSubstitutor(importTracker::use, "$[", "]", '\\');
        return code -> importTracker.applyImports(classSubstitutor.replace(code), importFormatter);
    }
}
