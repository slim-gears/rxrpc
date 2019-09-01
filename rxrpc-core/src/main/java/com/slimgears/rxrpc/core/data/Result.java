package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue()
public abstract class Result {
    public enum Type {
        Data,
        Complete,
        Error
    }

    @JsonProperty public abstract Type type();
    @Nullable @JsonProperty public abstract JsonNode data();
    @Nullable @JsonProperty public abstract ErrorInfo error();

    @JsonCreator
    static Result create(
            @JsonProperty("type") Type type,
            @JsonProperty("data") JsonNode data,
            @JsonProperty("error") ErrorInfo error) {
        return new AutoValue_Result(type, data, error);
    }

    public static Result ofData(JsonNode data) {
        return new AutoValue_Result(Type.Data, data, null);
    }

    public static Result ofComplete() {
        return new AutoValue_Result(Type.Complete, null, null);
    }

    public static Result ofError(Throwable error) {
        return ofError(ErrorInfos.fromException(error));
    }

    public static Result ofError(ErrorInfo error) {
        return new AutoValue_Result(Type.Error, null, error);
    }
}
