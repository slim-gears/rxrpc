package com.slimgears.rxrpc.gson;

import com.google.gson.JsonPrimitive;

import javax.json.JsonValue;

public class GsonJsonBoolean implements JsonValue {
    private final ValueType valueType;
    private final JsonPrimitive gsonPrimitive;

    public static JsonValue of(JsonPrimitive jsonPrimitive) {
        return new GsonJsonBoolean(jsonPrimitive);
    }

    private GsonJsonBoolean(JsonPrimitive gsonPrimitive) {
        this.gsonPrimitive = gsonPrimitive;
        assert gsonPrimitive.isBoolean();
        valueType = gsonPrimitive.getAsBoolean() ? ValueType.TRUE : ValueType.FALSE;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return gsonPrimitive.toString();
    }
}
