package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoOneOf;

@AutoOneOf(Result.Type.class)
public abstract class Result {
    public enum Type {
        Data,
        Complete,
        Error
    }

    public static class Void {
        static final Void Value = new Void();
        private Void() {}
    }

    @JsonProperty public abstract Type type();
    @JsonProperty public abstract JsonNode data();
    @JsonProperty public abstract Void complete();
    @JsonProperty public abstract ErrorInfo error();

    public static Result ofData(JsonNode data) {
        return AutoOneOf_Result.data(data);
    }

    public static Result ofComplete() {
        return AutoOneOf_Result.complete(Void.Value);
    }

    public static Result ofError(Throwable error) {
        return AutoOneOf_Result.error(ErrorInfo.fromException(error));
    }
}
