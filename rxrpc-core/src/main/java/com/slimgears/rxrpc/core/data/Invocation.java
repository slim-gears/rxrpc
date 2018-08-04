package com.slimgears.rxrpc.core.data;

import com.google.auto.value.AutoValue;

import javax.json.JsonObject;

@AutoValue
public abstract class Invocation {
    public abstract long invocationId();
    public abstract String method();
    public abstract JsonObject arguments();

    public static Builder builder() {
        return new AutoValue_Invocation.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder invocationId(long id);
        Builder method(String method);
        Builder arguments(JsonObject args);
        Invocation build();
    }
}
