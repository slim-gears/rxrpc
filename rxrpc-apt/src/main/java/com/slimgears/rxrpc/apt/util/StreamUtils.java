/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T, R extends T> Stream<R> ofType(Class<R> clazz, Stream<T> source) {
        return source.filter(clazz::isInstance).map(clazz::cast);
    }

    public static <T, R extends T> Function<T, Stream<R>> ofType(Class<R> clazz) {
        return item -> ofType(clazz, Stream.of(item));
    }

    public static <T> Function<T, T> self() {
        return s -> s;
    }
}
