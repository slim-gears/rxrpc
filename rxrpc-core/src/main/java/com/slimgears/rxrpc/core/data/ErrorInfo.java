package com.slimgears.rxrpc.core.data;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AutoValue
public abstract class ErrorInfo {
    public abstract String type();
    public abstract String message();
    public abstract List<String> stackTrace();
    @Nullable public abstract ErrorInfo cause();

    public static ErrorInfo create(String type, String message, String[] stackTrace) {
        return new AutoValue_ErrorInfo(type, message, Arrays.asList(stackTrace), null);
    }

    public static ErrorInfo create(String type, String message, String[] stackTrace, ErrorInfo cause) {
        return new AutoValue_ErrorInfo(type, message, Arrays.asList(stackTrace), cause);
    }

    public static ErrorInfo fromException(Throwable e) {
        return create(
                e.getClass().toString(),
                e.getMessage(),
                toString(e.getStackTrace()),
                Optional.ofNullable(e.getCause()).map(ErrorInfo::fromException).orElse(null));
    }

    private static String[] toString(StackTraceElement[] elements) {
        return Stream.of(elements).map(ErrorInfo::toString).toArray(String[]::new);
    }

    private static String toString(StackTraceElement element) {
        return String.format("%s.%s (%s:%d)", element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber());
    }
}
