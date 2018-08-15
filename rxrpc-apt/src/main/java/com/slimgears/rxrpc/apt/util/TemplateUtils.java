/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.google.common.collect.ImmutableSet;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.function.Function;
import java.util.regex.Pattern;

public class TemplateUtils {
    public final static TemplateUtils instance = new TemplateUtils();
    private final static Pattern camelCasePattern = Pattern.compile("([a-z])([A-Z])");
    private final static ImmutableSet<String> knownAsyncTypes = ImmutableSet.<String>builder()
            .add("io.reactivex.Observable")
            .add("io.reactivex.Single")
            .add("io.reactivex.Maybe")
            .add("io.reactivex.Completable")
            .add("java.util.concurrent.Future")
            .build();

    public static boolean isKnownAsyncType(TypeInfo type) {
        return knownAsyncTypes.contains(type.name());
    }

    public static String camelCaseToDash(String camelCase) {
        return camelCasePattern.matcher(camelCase).replaceAll("$1-$2").toLowerCase();
    }

    public static Function<String, String> postProcessImports(ImportTracker importTracker) {
        return code -> substituteImports(importTracker, code);
    }

    private static String substituteImports(ImportTracker importTracker, String code) {
        StringBuilder builder = new StringBuilder();
        int len = code.length();
        int prevPos = 0;
        int pos = 0;
        while ((pos = code.indexOf("$[", pos)) >= 0) {
            int count = 1;
            int endPos = pos + 2;
            while (count > 0 && endPos < len) {
                if (code.charAt(endPos) == '[') ++count;
                else if (code.charAt(endPos) == ']') --count;
                ++endPos;
            }

            String type = code.substring(pos + 2, endPos - 1);
            builder.append(code, prevPos, pos);
            builder.append(importTracker.use(type));
            prevPos = pos = endPos;
        }
        builder.append(code, prevPos, len);
        return builder.toString();
    }
}
