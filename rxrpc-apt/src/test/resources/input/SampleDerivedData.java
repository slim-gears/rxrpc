package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class SampleDerivedData implements SampleBaseData {
    @Override
    public abstract int value();

    @Override @JsonIgnore
    public abstract int ignoredMethod();
}
