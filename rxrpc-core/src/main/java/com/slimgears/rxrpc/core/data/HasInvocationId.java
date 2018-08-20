/**
 *
 */
package com.slimgears.rxrpc.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface HasInvocationId {
    @JsonProperty long invocationId();

    interface Builder<B extends Builder<B>> {
        B invocationId(long id);
    }
}
