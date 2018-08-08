/**
 *
 */
package com.slimgears.rxrpc.apt;

import org.apache.commons.text.StringSubstitutor;

import java.util.function.Function;

public class PostProcessors {
    public static Function<String, String> applyJavaImports(ImportTracker importTracker) {
        return applyImports(importTracker, imp -> "import " + imp + ";");
    }

    private static Function<String, String> applyImports(ImportTracker importTracker, Function<String, String> importFormatter) {
        StringSubstitutor classSubstitutor = new StringSubstitutor(importTracker::use, "$[", "]", '\\');
        return code -> importTracker.applyImports(classSubstitutor.replace(code), importFormatter);
    }
}
