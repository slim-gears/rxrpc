/**
 *
 */
package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleRequest {
    @JsonProperty public final int id;
    @JsonProperty public final String name;
    @JsonProperty public final Class<?> type;

    @JsonCreator
    public SampleRequest(@JsonProperty("id") int id,
                         @JsonProperty("name") String name,
                         @JsonProperty("type") Class<?> type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
