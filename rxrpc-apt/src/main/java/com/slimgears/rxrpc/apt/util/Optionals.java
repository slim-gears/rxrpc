/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Optionals {
    @SafeVarargs
    public static <T> Optional<T> or(Supplier<Optional<T>>... variants) {
        return Stream.of(variants)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
