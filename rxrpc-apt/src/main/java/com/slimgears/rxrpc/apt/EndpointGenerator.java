/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.EndpointContext;

public interface EndpointGenerator {
    void generate(EndpointContext context);
}
