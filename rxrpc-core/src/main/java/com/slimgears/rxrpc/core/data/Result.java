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

import java.io.IOException;

@AutoOneOf(Result.Type.class)
@JsonSerialize(using = Result.Serializer.class)
public abstract class Result {
    public enum Type {
        Data,
        Complete,
        Error
    }

    public static class Void {
        static final Void Value = new Void();
        private Void() {}

        @JsonCreator
        public Void value() {
            return Value;
        }
    }

    @JsonProperty public abstract Type type();
    @JsonProperty public abstract JsonNode data();
    @JsonProperty public abstract Void complete();
    @JsonProperty public abstract ErrorInfo error();

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
        return AutoOneOf_Result.data(data);
    }

    public static Result ofComplete() {
        return AutoOneOf_Result.complete(Void.Value);
    }

    public static Result ofError(Throwable error) {
        return ofError(ErrorInfo.fromException(error));
    }

    public static Result ofError(ErrorInfo error) {
        return AutoOneOf_Result.error(error);
    }

    public static class Serializer extends JsonSerializer<Result> {
        @Override
        public void serialize(Result value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeFieldName("type"); gen.writeObject(value.type());
            switch (value.type()) {
                case Data:
                    gen.writeFieldName("data");
                    value.data().serialize(gen, serializers);
                    break;
                case Error:
                    gen.writeFieldName("error");
                    gen.writeObject(value.error());
                    break;
                case Complete:
                    break;
            }
            gen.writeEndObject();
        }
    }
}
