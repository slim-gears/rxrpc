package com.slimgears.rxrpc.gson;

import com.google.gson.JsonElement;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GsonJsonObject implements JsonObject {
    private final com.google.gson.JsonObject gsonObject;

    public static JsonObject of(com.google.gson.JsonObject gsonObject) {
        return new GsonJsonObject(gsonObject);
    }

    private GsonJsonObject(com.google.gson.JsonObject gsonObject) {
        this.gsonObject = gsonObject;
    }

    @Override
    public JsonArray getJsonArray(String name) {
        return GsonJsonArray.of(gsonObject.getAsJsonArray(name));
    }

    @Override
    public JsonObject getJsonObject(String name) {
        return GsonJsonObject.of(gsonObject.getAsJsonObject(name));
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        return GsonJsonNumber.of(gsonObject.getAsJsonPrimitive(name));
    }

    @Override
    public JsonString getJsonString(String name) {
        return GsonJsonString.of(gsonObject.getAsJsonPrimitive(name));
    }

    @Override
    public String getString(String name) {
        return gsonObject.getAsJsonPrimitive(name).getAsString();
    }

    @Override
    public String getString(String name, String defaultValue) {
        return Optional.ofNullable(gsonObject.get(name)).map(JsonElement::getAsString).orElse(defaultValue);
    }

    @Override
    public int getInt(String name) {
        return gsonObject.getAsJsonPrimitive(name).getAsInt();
    }

    @Override
    public int getInt(String name, int defaultValue) {
        return Optional.ofNullable(gsonObject.get(name)).map(JsonElement::getAsInt).orElse(defaultValue);
    }

    @Override
    public boolean getBoolean(String name) {
        return gsonObject.getAsJsonPrimitive(name).getAsBoolean();
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return Optional.ofNullable(gsonObject.get(name)).map(JsonElement::getAsBoolean).orElse(defaultValue);
    }

    @Override
    public boolean isNull(String name) {
        return gsonObject.get(name).isJsonNull();
    }

    @Override
    public int size() {
        return gsonObject.size();
    }

    @Override
    public boolean isEmpty() {
        return gsonObject.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return gsonObject.has((String)key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonValue get(Object key) {
        return GsonJson.fromElement(gsonObject.get((String)key));
    }

    @Override
    public JsonValue put(String key, JsonValue value) {
        JsonElement gsonElement = gsonObject.get(key);
        gsonObject.add(key, GsonJson.toElement(value));
        return Optional.ofNullable(gsonElement).map(GsonJson::fromElement).orElse(null);
    }

    @Override
    public JsonValue remove(Object key) {
        return Optional.ofNullable(gsonObject.remove((String)key)).map(GsonJson::fromElement).orElse(null);
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        gsonObject.entrySet().clear();
    }

    @Override
    public Set<String> keySet() {
        return gsonObject.keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        return gsonObject.entrySet()
                .stream()
                .map(e -> GsonJson.fromElement(e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        return gsonObject
                .entrySet()
                .stream()
                .collect(Collectors.toMap((Entry<String, JsonElement> e) -> e.getKey(), (Entry<String, JsonElement> e) -> GsonJson.fromElement(e.getValue())))
                .entrySet();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @Override
    public String toString() {
        return gsonObject.toString();
    }
}
