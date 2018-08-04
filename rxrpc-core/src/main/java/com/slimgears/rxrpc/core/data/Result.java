package com.slimgears.rxrpc.core.data;

import com.google.auto.value.AutoOneOf;

import javax.json.JsonValue;

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

    public abstract Type type();
    public abstract JsonValue data();
    public abstract Void complete();
    public abstract ErrorInfo error();

    public static Result ofData(JsonValue data) {
        return AutoOneOf_Result.data(data);
    }

    public static Result ofComplete() {
        return AutoOneOf_Result.complete(Void.Value);
    }

    public static Result ofError(Throwable error) {
        return AutoOneOf_Result.error(ErrorInfo.fromException(error));
    }
}
