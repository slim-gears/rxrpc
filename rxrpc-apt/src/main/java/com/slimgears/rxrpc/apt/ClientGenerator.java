/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.EndpointContext;

public interface ClientGenerator {
    void generateClient(EndpointContext context);
}
