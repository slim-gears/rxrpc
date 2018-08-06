package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Invocation {
    @JsonProperty public abstract long invocationId();
    @JsonProperty public abstract String method();
    @JsonProperty public abstract Map<String, Object> arguments();

    @JsonCreator
    public static Invocation create(
            @JsonProperty("invocationId") long invocationId,
            @JsonProperty("method") String method,
            @JsonProperty("arguments") Map<String, Object> arguments) {
        return Invocation.builder()
                .invocationId(invocationId)
                .method(method)
                .arguments(arguments)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Invocation.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder invocationId(long id);
        Builder method(String method);
        Builder arguments(Map<String, Object> args);
        Invocation build();
    }
}
