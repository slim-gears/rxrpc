package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SampleInterfaceWithIgnoredMethod {
    @JsonIgnore int ignoredMethod();
}
