package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AutoValue
public abstract class ErrorInfo {
    @JsonProperty public abstract String type();
    @JsonProperty @Nullable public abstract String message();
    @JsonProperty public abstract List<String> stackTrace();
    @JsonProperty @Nullable public abstract ErrorInfo cause();

    public static ErrorInfo create(String type, String message, String[] stackTrace) {
        return new AutoValue_ErrorInfo(type, message, Arrays.asList(stackTrace), null);
    }

    @SuppressWarnings("unchecked")
    public Throwable toException() {
        try {
            Class<? extends Throwable> cls = (Class<? extends Throwable>)Class.forName(type());
            ErrorInfo cause = cause();
            if (cause != null) {
                Constructor<? extends Throwable> ctor = cls.getConstructor(String.class, Throwable.class);
                return ctor.newInstance(message(), cause.toException());
            } else {
                Constructor<? extends Throwable> ctor = cls.getConstructor(String.class);
                return ctor.newInstance(message());
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return new RuntimeException(message() + "\n" + String.join("\n", stackTrace()));
        }
    }

    @JsonCreator
    public static ErrorInfo create(
            @JsonProperty("type") String type,
            @JsonProperty("message") String message,
            @JsonProperty("stackTrace") String[] stackTrace,
            @JsonProperty("cause") ErrorInfo cause) {
        return new AutoValue_ErrorInfo(type, message, Arrays.asList(stackTrace), cause);
    }

    public static ErrorInfo fromException(Throwable e) {
        return create(
                e.getClass().getCanonicalName(),
                e.getMessage(),
                toStringArray(e.getStackTrace()),
                Optional.ofNullable(e.getCause()).map(ErrorInfo::fromException).orElse(null));
    }

    private static String[] toStringArray(StackTraceElement[] elements) {
        return Stream.of(elements).map(ErrorInfo::toStringArray).toArray(String[]::new);
    }

    private static String toStringArray(StackTraceElement element) {
        return String.format("%s.%s (%s:%d)", element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber());
    }
}
