/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import java.util.Properties;

public interface ConfigProvider {
    void apply(Properties properties);
}
