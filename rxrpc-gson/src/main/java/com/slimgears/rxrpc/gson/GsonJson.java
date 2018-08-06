package com.slimgears.rxrpc.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class GsonJson {
    public static JsonPrimitive toPrimitive(JsonNumber number) {
        return new JsonPrimitive(number.bigDecimalValue());
    }

    public static JsonPrimitive toPrimitive(JsonString string) {
        return new JsonPrimitive(string.getString());
    }

    public static JsonElement toElement(JsonValue value) {
        switch (value.getValueType()) {
            case NUMBER: return toPrimitive((JsonNumber)value);
            case STRING: return toPrimitive((JsonString)value);
            case TRUE: return new JsonPrimitive(true);
            case FALSE: return new JsonPrimitive(false);
            case NULL: return JsonNull.INSTANCE;
            case OBJECT: return toObject((JsonObject)value);
            case ARRAY: return toArray((JsonArray)value);
        }

        throw new RuntimeException("Not recognized value: " + value);
    }

    public static com.google.gson.JsonObject toObject(JsonObject object) {
        com.google.gson.JsonObject gsonObj = new com.google.gson.JsonObject();
        object.forEach((name, value) -> gsonObj.add(name, toElement(value)));
        return gsonObj;
    }

    public static com.google.gson.JsonArray toArray(JsonArray array) {
        com.google.gson.JsonArray gsonArray = new com.google.gson.JsonArray(array.size());
        array.forEach(value -> gsonArray.add(toElement(value)));
        return gsonArray;
    }

    public static JsonValue fromElement(JsonElement gsonElement) {
        if (gsonElement.isJsonNull()) {
            return GsonJsonNull.INSTANCE;
        }

        if (gsonElement.isJsonPrimitive()) {
            JsonPrimitive gsonPrimitive = gsonElement.getAsJsonPrimitive();
            if (gsonPrimitive.isNumber()) {
                return GsonJsonNumber.of(gsonPrimitive);
            } else if (gsonPrimitive.isString()) {
                return GsonJsonString.of(gsonPrimitive);
            } else if (gsonPrimitive.isBoolean()) {
                return GsonJsonBoolean.of(gsonPrimitive);
            }
        } else if (gsonElement.isJsonObject()) {
            return GsonJsonObject.of(gsonElement.getAsJsonObject());
        } else if (gsonElement.isJsonArray()) {
            return GsonJsonArray.of(gsonElement.getAsJsonArray());
        }

        throw new RuntimeException("Not recognized GSON: " + gsonElement);
    }
}
