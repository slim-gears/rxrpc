package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Map;

@AutoValue
public abstract class Invocation {
    @JsonProperty public abstract long invocationId();
    @Nullable @JsonProperty public abstract String method();
    @Nullable @JsonProperty public abstract Map<String, JsonNode> arguments();

    @JsonCreator
    public static Invocation create(
            @JsonProperty("invocationId") long invocationId,
            @JsonProperty("method") String method,
            @JsonProperty("arguments") Map<String, JsonNode> arguments) {
        return Invocation.builder()
                .invocationId(invocationId)
                .method(method)
                .arguments(arguments)
                .build();
    }

    public static Invocation ofCancellation(long invocationId) {
        return create(invocationId, null, null);
    }

    public static Builder builder() {
        return new AutoValue_Invocation.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder invocationId(long id);
        Builder method(String method);
        Builder arguments(Map<String, JsonNode> args);
        Invocation build();
    }
}
