package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Response {
    @JsonProperty public abstract long invocationId();
    @JsonProperty public abstract Result result();

    public static Response ofData(long invocationId, JsonNode data) {
        return new AutoValue_Response(invocationId, Result.ofData(data));
    }

    public static Response ofComplete(long invocationId) {
        return new AutoValue_Response(invocationId, Result.ofComplete());
    }

    public static Response ofError(long invocationId, Throwable error) {
        return new AutoValue_Response(invocationId, Result.ofError(error));
    }

}
