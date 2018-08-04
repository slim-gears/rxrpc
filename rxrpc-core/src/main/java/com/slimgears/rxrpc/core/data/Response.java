package com.slimgears.rxrpc.core.data;

import com.google.auto.value.AutoValue;

import javax.json.JsonValue;

@AutoValue
public abstract class Response {
    public abstract long invocationId();
    public abstract Result result();

    public static Response ofData(long invocationId, JsonValue data) {
        return new AutoValue_Response(invocationId, Result.ofData(data));
    }

    public static Response ofComplete(long invocationId) {
        return new AutoValue_Response(invocationId, Result.ofComplete());
    }

    public static Response ofError(long invocationId, Throwable error) {
        return new AutoValue_Response(invocationId, Result.ofError(error));
    }

}
