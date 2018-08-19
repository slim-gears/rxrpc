package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleNotification {
    @JsonProperty public final String data;
    @JsonProperty public final long sequenceNum;

    @JsonCreator
    public SampleNotification(@JsonProperty("data") String data, @JsonProperty("sequenceNum") long sequenceNum) {
        this.data = data;
        this.sequenceNum = sequenceNum;
    }
}
