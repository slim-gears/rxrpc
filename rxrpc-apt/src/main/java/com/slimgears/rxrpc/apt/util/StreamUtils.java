/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.util;

import java.util.stream.Stream;

public class StreamUtils {
    public static <T, R> Stream<R> ofType(Class<R> clazz, Stream<T> source) {
        return source.filter(clazz::isInstance).map(clazz::cast);
    }
}
