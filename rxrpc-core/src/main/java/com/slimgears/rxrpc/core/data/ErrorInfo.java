package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@AutoValue
public abstract class ErrorInfo {
    @JsonProperty public abstract String type();
    @JsonProperty @Nullable public abstract String message();
    @JsonProperty public abstract List<String> stackTrace();
    @JsonProperty @Nullable public abstract ErrorInfo cause();
    @JsonProperty @Nullable public abstract Map<String, Object> properties();

    public static ErrorInfo create(String type, String message, String[] stackTrace, Map<String, Object> properties) {
        return new AutoValue_ErrorInfo(type, message, Arrays.asList(stackTrace), null, properties);
    }

    public Throwable toException() {
        return ErrorInfos.toException(this);
    }

    @JsonCreator
    public static ErrorInfo create(
            @JsonProperty("type") String type,
            @JsonProperty("message") String message,
            @JsonProperty("stackTrace") String[] stackTrace,
            @JsonProperty("cause") ErrorInfo cause,
            @JsonProperty("properties") Map<String, Object> properties) {
        return new AutoValue_ErrorInfo(type, message, Arrays.asList(stackTrace), cause, properties);
    }

}
