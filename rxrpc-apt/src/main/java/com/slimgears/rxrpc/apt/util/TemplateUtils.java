/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.google.common.collect.ImmutableSet;
import com.slimgears.apt.data.AnnotationValueInfo;
import com.slimgears.apt.data.MethodInfo;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.rxrpc.core.RxRpcMethod;

import java.util.Optional;
import java.util.regex.Pattern;

import static com.slimgears.util.stream.Optionals.ofType;

public class TemplateUtils extends com.slimgears.apt.util.TemplateUtils {
    public final static TemplateUtils instance = new TemplateUtils();
    private final static Pattern camelCasePattern = Pattern.compile("([a-z])([A-Z])");
    private final static ImmutableSet<String> knownAsyncTypes = ImmutableSet.<String>builder()
            .add("io.reactivex.Observable")
            .add("io.reactivex.Single")
            .add("io.reactivex.Maybe")
            .add("io.reactivex.Completable")
            .add("io.reactivex.Flowable")
            .add("org.reactivestreams.Publisher")
            .add("java.util.concurrent.Future")
            .build();

    public static boolean isKnownAsyncType(TypeInfo type) {
        return knownAsyncTypes.contains(type.name());
    }

    public static TypeInfo elementType(TypeInfo type) {
        return isKnownAsyncType(type) ? type.elementTypeOrVoid() : type.elementTypeOrSelf();
    }

    public boolean isShared(MethodInfo method) {
        return Optional
                .ofNullable(method.getAnnotation(RxRpcMethod.class))
                .map(ai -> ai.getValue("shared"))
                .map(AnnotationValueInfo.Value::asString)
                .map("true"::equals)
                .orElse(false);
    }

    public int getSharedReplayCount(MethodInfo method) {
        return Optional
                .ofNullable(method.getAnnotation(RxRpcMethod.class))
                .map(ai -> ai.getValue("sharedReplayCount"))
                .map(AnnotationValueInfo.Value::primitive)
                .flatMap(ofType(Integer.class))
                .orElse(0);
    }


    public static String camelCaseToDash(String camelCase) {
        return camelCasePattern.matcher(camelCase).replaceAll("$1-$2").toLowerCase();
    }
}
