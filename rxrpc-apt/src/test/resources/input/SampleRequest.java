package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.slimgears.rxrpc.sample.SampleData;

import java.util.Map;

public class SampleRequest {
    public final int id;

    @JsonProperty public final String name;

    public SampleData getData() {
        return null;
    }

    public Map<String, JsonNode> mapData() {
        return null;
    }

    public SampleData[] multipleData() { return new SampleData[0]; }

    public SampleRequest(@JsonProperty("id") int id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }
}
