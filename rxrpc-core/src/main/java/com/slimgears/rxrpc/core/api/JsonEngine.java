package com.slimgears.rxrpc.core.api;

import javax.json.JsonArray;
import javax.json.JsonValue;

public interface JsonEngine {
    <T> JsonValue encode(T obj);
    <T> JsonArray encode(T[] array);

    default String toString(JsonValue jsonValue) {
        return jsonValue.toString();
    }

    <T> T decode(JsonValue jsonValue, Class<T> cls);
    <T> T[] decode(JsonArray jsonArray, Class<T> cls);

    JsonValue fromString(String str);

    default <T> T decodeString(String str, Class<T> cls) {
        return decode(fromString(str), cls);
    }

    default <T> String encodeString(T[] array) {
        return toString(encode(array));
    }

    default <T> String encodeString(T obj) {
        return toString(encode(obj));
    }
}
