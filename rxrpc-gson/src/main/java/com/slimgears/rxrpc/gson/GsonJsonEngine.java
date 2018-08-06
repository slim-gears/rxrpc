package com.slimgears.rxrpc.gson;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.slimgears.rxrpc.core.api.JsonEngine;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.lang.reflect.Array;

@AutoService(JsonEngine.class)
public class GsonJsonEngine implements JsonEngine {
    private final Gson gson;

    public GsonJsonEngine() {
        this(new Gson());
    }

    public GsonJsonEngine(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> JsonValue encode(T obj) {
        return GsonJson.fromElement(gson.toJsonTree(obj));
    }

    @Override
    public <T> JsonArray encode(T[] array) {
        return GsonJson.fromElement(gson.toJsonTree(array)).asJsonArray();
    }

    @Override
    public <T> T decode(JsonValue jsonValue, Class<T> cls) {
        return gson.fromJson(GsonJson.toElement(jsonValue), cls);
    }

    @Override
    public <T> T[] decode(JsonArray jsonArray, Class<T> cls) {
        //noinspection unchecked
        return jsonArray.getValuesAs(JsonValue.class)
                .stream()
                .map(val -> decode(val, cls))
                .toArray(size -> (T[])Array.newInstance(cls, size));
    }

    @Override
    public JsonValue fromString(String str) {
        return GsonJson.fromElement(gson.fromJson(str, JsonElement.class));
    }
}
