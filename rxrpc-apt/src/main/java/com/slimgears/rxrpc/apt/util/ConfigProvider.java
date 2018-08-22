/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.util;

import java.util.Properties;

public interface ConfigProvider {
    void apply(Properties properties);
}
