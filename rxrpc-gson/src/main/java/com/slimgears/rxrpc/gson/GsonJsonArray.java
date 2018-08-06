package com.slimgears.rxrpc.gson;

import com.google.gson.JsonElement;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GsonJsonArray implements JsonArray {
    private final com.google.gson.JsonArray gsonArray;

    public static JsonArray of(com.google.gson.JsonArray gsonArray) {
        return new GsonJsonArray(gsonArray);
    }

    private GsonJsonArray(com.google.gson.JsonArray gsonArray) {
        this.gsonArray = gsonArray;
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return GsonJson.fromElement(gsonArray.get(index)).asJsonObject();
    }

    @Override
    public JsonArray getJsonArray(int index) {
        return GsonJson.fromElement(gsonArray.get(index)).asJsonArray();
    }

    @Override
    public JsonNumber getJsonNumber(int index) {
        return (JsonNumber) GsonJson.fromElement(gsonArray.get(index));
    }

    @Override
    public JsonString getJsonString(int index) {
        return (JsonString) GsonJson.fromElement(gsonArray.get(index));
    }

    @Override
    public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
        return this.streamItems().map(clazz::cast).collect(Collectors.toList());
    }

    @Override
    public String getString(int index) {
        return gsonArray.get(index).getAsString();
    }

    @Override
    public String getString(int index, String defaultValue) {
        return getAt(JsonElement::getAsString, index, defaultValue);
    }

    @Override
    public int getInt(int index) {
        return gsonArray.get(index).getAsInt();
    }

    @Override
    public int getInt(int index, int defaultValue) {
        return getAt(JsonElement::getAsInt, index, defaultValue);
    }

    @Override
    public boolean getBoolean(int index) {
        return gsonArray.get(index).getAsBoolean();
    }

    @Override
    public boolean getBoolean(int index, boolean defaultValue) {
        return getAt(JsonElement::getAsBoolean, index, defaultValue);
    }

    @Override
    public boolean isNull(int index) {
        return gsonArray.get(index).isJsonNull();
    }

    @Override
    public int size() {
        return gsonArray.size();
    }

    @Override
    public boolean isEmpty() {
        return gsonArray.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return streamItems().anyMatch(val -> val.equals(o));
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return streamItems().iterator();
    }

    @Override
    public Object[] toArray() {
        return streamItems().toArray(JsonValue[]::new);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        //noinspection unchecked
        return streamItems().map(val -> (T)val).collect(Collectors.toList()).toArray(a);
    }

    @Override
    public boolean add(JsonValue jsonValue) {
        gsonArray.add(GsonJson.toElement(jsonValue));
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends JsonValue> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends JsonValue> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int sizeBefore = size();
        c.forEach(this::remove);
        return sizeBefore != size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonValue get(int index) {
        return GsonJson.fromElement(gsonArray.get(index));
    }

    @Override
    public JsonValue set(int index, JsonValue element) {
        JsonValue prevItem = Optional.ofNullable(gsonArray.get(index)).map(GsonJson::fromElement).orElse(null);
        gsonArray.set(index, GsonJson.toElement(element));
        return prevItem;
    }

    @Override
    public void add(int index, JsonValue element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonValue remove(int index) {
        JsonValue prevItem = Optional.ofNullable(gsonArray.get(index)).map(GsonJson::fromElement).orElse(null);
        gsonArray.remove(index);
        return prevItem;
    }

    @Override
    public int indexOf(Object o) {
        return getValuesAs(JsonValue.class).indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getValuesAs(JsonValue.class).lastIndexOf(o);
    }

    @Override
    public ListIterator<JsonValue> listIterator() {
        return getValuesAs(JsonValue.class).listIterator();
    }

    @Override
    public ListIterator<JsonValue> listIterator(int index) {
        return getValuesAs(JsonValue.class).listIterator(index);
    }

    @Override
    public List<JsonValue> subList(int fromIndex, int toIndex) {
        return getValuesAs(JsonValue.class).subList(fromIndex, toIndex);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    private <T> T getAt(Function<JsonElement, T> getter, int index, T defaultValue) {
        return Optional
                .ofNullable(gsonArray.get(index))
                .filter(v -> !v.isJsonNull())
                .map(getter)
                .orElse(defaultValue);
    }

    private Stream<JsonValue> streamItems() {
        return StreamSupport
                .stream(gsonArray.spliterator(), false)
                .map(GsonJson::fromElement);
    }

    @Override
    public String toString() {
        return gsonArray.toString();
    }
}
