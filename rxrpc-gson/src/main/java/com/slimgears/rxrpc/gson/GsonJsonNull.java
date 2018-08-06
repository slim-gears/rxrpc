package com.slimgears.rxrpc.gson;

import com.google.gson.JsonNull;

import javax.json.JsonValue;

public class GsonJsonNull implements JsonValue {
    public final static JsonValue INSTANCE = new GsonJsonNull();

    private GsonJsonNull() {

    }

    @Override
    public ValueType getValueType() {
        return ValueType.NULL;
    }

    @Override
    public String toString() {
        return JsonNull.INSTANCE.toString();
    }
}
