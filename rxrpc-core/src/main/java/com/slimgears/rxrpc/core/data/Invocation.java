package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@AutoValue
public abstract class Invocation implements HasInvocationId {
    public enum Type {
        Subscription,
        Unsubscription,
        KeepAlive
    }

    @JsonProperty public abstract Type type();
    @Nullable @JsonProperty public abstract String method();
    @Nullable @JsonProperty public abstract Map<String, JsonNode> arguments();

    @JsonIgnore public boolean is(Type type) {
        return type() == type;
    }
    @JsonIgnore public boolean isSubscription() {
        return is(Type.Subscription);
    }
    @JsonIgnore public boolean isUnsubscription() {
        return is(Type.Unsubscription);
    }
    @JsonIgnore public boolean isKeepAlive() {
        return is(Type.KeepAlive);
    }

    @JsonCreator
    public static Invocation create(
            @JsonProperty("type") Type type,
            @JsonProperty("invocationId") long invocationId,
            @JsonProperty("method") String method,
            @JsonProperty("arguments") Map<String, JsonNode> arguments) {
        return Invocation.builder()
                .type(type)
                .invocationId(invocationId)
                .method(method)
                .arguments(arguments)
                .build();
    }

    public static Invocation ofUnsubscription(long invocationId) {
        return create(Type.Unsubscription, invocationId, null, null);
    }

    public static Invocation ofSubscription(long invocationId, String method, Map<String, JsonNode> args) {
        return create(Type.Subscription, invocationId, requireNonNull(method), requireNonNull(args));
    }

    public static Invocation ofKeepAlive() {
        return create(Type.KeepAlive, -1, null, null);
    }

    public static Builder builder() {
        return new AutoValue_Invocation.Builder();
    }

    @AutoValue.Builder
    public interface Builder extends HasInvocationId.Builder<Builder> {
        Builder type(Type type);
        Builder method(String method);
        Builder arguments(Map<String, JsonNode> args);
        Invocation build();
    }
}
