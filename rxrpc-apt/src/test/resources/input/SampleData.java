package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slimgears.rxrpc.core.RxRpcData;

@RxRpcData
public class SampleData {
    public boolean isFinished() {
        return false;
    }
}
