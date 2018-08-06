package com.slimgears.rxrpc.gson;

import com.google.gson.JsonPrimitive;

import javax.json.JsonString;

public class GsonJsonString implements JsonString {
    private final JsonPrimitive gsonPrimitive;

    public static JsonString of(JsonPrimitive gsonPrimitive) {
        return new GsonJsonString(gsonPrimitive);
    }

    private GsonJsonString(JsonPrimitive gsonPrimitive) {
        assert gsonPrimitive.isString();
        this.gsonPrimitive = gsonPrimitive;
    }

    @Override
    public String getString() {
        return gsonPrimitive.getAsString();
    }

    @Override
    public CharSequence getChars() {
        return getString();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    @Override
    public String toString() {
        return gsonPrimitive.toString();
    }
}
