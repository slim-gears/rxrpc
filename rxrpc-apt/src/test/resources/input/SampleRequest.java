/**
 *
 */
package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slimgears.rxrpc.core.RxRpcData;
import com.slimgears.rxrpc.sample.SampleData;

@RxRpcData
public class SampleRequest {
    public final int id;
    @JsonProperty public final String name;
    public SampleData getData() {
        return null;
    }

    public SampleRequest(@JsonProperty("id") int id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }
}
