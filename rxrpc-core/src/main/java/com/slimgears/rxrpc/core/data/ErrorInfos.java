package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ErrorInfos {
    public static ErrorInfo fromException(Throwable e) {
        return ErrorInfo.create(
                e.getClass().getCanonicalName(),
                e.getMessage(),
                toStringArray(e.getStackTrace()),
                Optional.ofNullable(e.getCause()).map(ErrorInfos::fromException).orElse(null),
                propertiesFromException(e));
    }

    @SuppressWarnings("unchecked")
    public static Throwable toException(ErrorInfo errorInfo) {
        try {
            Class<? extends Throwable> cls = (Class<? extends Throwable>)Class.forName(errorInfo.type());
            ErrorInfo cause = errorInfo.cause();
            if (cause != null) {
                Constructor<? extends Throwable> ctor = cls.getConstructor(String.class, Throwable.class);
                return ctor.newInstance(errorInfo.message(), cause.toException());
            } else {
                Constructor<? extends Throwable> ctor = cls.getConstructor(String.class);
                return ctor.newInstance(errorInfo.message());
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return new RxRpcRemoteException(errorInfo);
        }
    }

    private static String[] toStringArray(StackTraceElement[] elements) {
        return Stream.of(elements).map(ErrorInfos::toStringArray).toArray(String[]::new);
    }

    private static String toStringArray(StackTraceElement element) {
        return String.format("%s.%s (%s:%d)", element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map<String, Object> propertiesFromException(Throwable e) {
        Class<?> cls = e.getClass();
        Map<String, Object> props = Arrays.stream(cls.getMethods())
                .filter(m -> m.getParameterCount() == 0 &&
                        m.getReturnType() != void.class &&
                        m.getAnnotation(JsonProperty.class) != null)
                .collect(ImmutableMap.toImmutableMap(ErrorInfos::toPropertyName, m -> toPropertyValue(m, e)));
        return !props.isEmpty() ? props : null;
    }

    private static Object toPropertyValue(Method method, Object instance) {
        try {
            return method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static String toPropertyName(Method method) {
        String name = Optional.ofNullable(method.getAnnotation(JsonProperty.class))
                .map(JsonProperty::value)
                .filter(n -> !n.isEmpty())
                .orElseGet(() -> method.getName().replaceFirst("get([A-Z])", "$1"));
        return !name.isEmpty() && Character.isUpperCase(name.charAt(0))
                ? Character.toLowerCase(name.charAt(0)) + name.substring(1)
                : name;
    }
}
