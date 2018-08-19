/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface HasInvocationId {
    @JsonProperty long invocationId();

    interface Builder<B extends Builder<B>> {
        B invocationId(long id);
    }
}
