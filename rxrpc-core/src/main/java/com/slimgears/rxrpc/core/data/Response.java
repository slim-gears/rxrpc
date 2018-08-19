package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Response implements HasInvocationId {
    @JsonProperty public abstract Result result();

    @JsonCreator
    public static Response create(
            @JsonProperty("invocationId") long invocationId,
            @JsonProperty("result") Result result) {
        return new AutoValue_Response(invocationId, result);
    }

    public static Response ofData(long invocationId, JsonNode data) {
        return create(invocationId, Result.ofData(data));
    }

    public static Response ofComplete(long invocationId) {
        return create(invocationId, Result.ofComplete());
    }

    public static Response ofError(long invocationId, Throwable error) {
        return create(invocationId, Result.ofError(error));
    }

}
