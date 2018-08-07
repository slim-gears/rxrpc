package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.google.auto.value.AutoOneOf;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.json.JsonValue;
import java.io.IOException;

@AutoValue()
public abstract class Result {
    public enum Type {
        Data,
        Complete,
        Error
    }

    @JsonProperty public abstract Type type();
    @Nullable @JsonProperty public abstract JsonNode data();
    @Nullable @JsonProperty public abstract ErrorInfo error();

    @JsonCreator
    static Result create(
            @JsonProperty("type") Type type,
            @JsonProperty("data") JsonNode data,
            @JsonProperty("error") ErrorInfo error) {
        switch (type) {
            case Data: return ofData(data);
            case Complete: return ofComplete();
            case Error: return ofError(error);
        }
        throw new IllegalArgumentException();
    }

    public static Result ofData(JsonNode data) {
        return new AutoValue_Result(Type.Data, data, null);
    }

    public static Result ofComplete() {
        return new AutoValue_Result(Type.Complete, null, null);
    }

    public static Result ofError(Throwable error) {
        return ofError(ErrorInfo.fromException(error));
    }

    public static Result ofError(ErrorInfo error) {
        return new AutoValue_Result(Type.Error, null, error);
    }
}
