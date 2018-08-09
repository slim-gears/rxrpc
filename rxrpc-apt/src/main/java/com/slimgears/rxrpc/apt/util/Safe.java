/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import java.util.function.Function;
import java.util.function.Supplier;

public class Safe {
    public interface UnsafeSupplier<T> {
        T get() throws Exception;
    }

    public interface UnsafeRunnable {
        void run() throws Exception;
    }

    public interface UnsafeFunction<T, R> {
        R apply(T from) throws Exception;
    }

    public static <T> Supplier<T> of(UnsafeSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable of(UnsafeRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, R> Function<T, R> of(UnsafeFunction<T, R> function) {
        return a -> {
            try {
                return function.apply(a);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
