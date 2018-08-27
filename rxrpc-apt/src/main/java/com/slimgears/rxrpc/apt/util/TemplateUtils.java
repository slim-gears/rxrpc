/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.google.common.collect.ImmutableSet;
import com.slimgears.apt.data.TypeInfo;

import java.util.function.Function;
import java.util.regex.Pattern;

public class TemplateUtils extends com.slimgears.apt.util.TemplateUtils {
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

    public static TypeInfo elementType(TypeInfo type) {
        return isKnownAsyncType(type) ? type.elementTypeOrVoid() : type.elementTypeOrSelf();
    }

    public static String camelCaseToDash(String camelCase) {
        return camelCasePattern.matcher(camelCase).replaceAll("$1-$2").toLowerCase();
    }
}
