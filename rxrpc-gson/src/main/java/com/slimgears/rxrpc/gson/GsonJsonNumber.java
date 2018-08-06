package com.slimgears.rxrpc.gson;

import com.google.gson.JsonPrimitive;

import javax.json.JsonNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

public class GsonJsonNumber implements JsonNumber {
    private final JsonPrimitive gsonPrimitive;

    public static JsonNumber of(JsonPrimitive gsonPrimitive) {
        return new GsonJsonNumber(gsonPrimitive);
    }

    private GsonJsonNumber(JsonPrimitive gsonPrimitive) {
        assert gsonPrimitive.isNumber();
        this.gsonPrimitive = gsonPrimitive;
    }

    @Override
    public boolean isIntegral() {
        return gsonPrimitive.getAsBigDecimal().scale() == 0;
    }

    @Override
    public int intValue() {
        return gsonPrimitive.getAsInt();
    }

    @Override
    public int intValueExact() {
        return gsonPrimitive.getAsBigInteger().intValueExact();
    }

    @Override
    public long longValue() {
        return gsonPrimitive.getAsLong();
    }

    @Override
    public long longValueExact() {
        return gsonPrimitive.getAsBigInteger().longValueExact();
    }

    @Override
    public BigInteger bigIntegerValue() {
        return gsonPrimitive.getAsBigInteger();
    }

    @Override
    public BigInteger bigIntegerValueExact() {
        return gsonPrimitive.getAsBigDecimal().toBigIntegerExact();
    }

    @Override
    public double doubleValue() {
        return gsonPrimitive.getAsDouble();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return gsonPrimitive.getAsBigDecimal();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }

    @Override
    public String toString() {
        return gsonPrimitive.toString();
    }
}
