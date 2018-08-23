/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import java.util.Collection;

public interface ServiceProvider {
    <T> Collection<T> loadServices(Class<T> cls);
}
