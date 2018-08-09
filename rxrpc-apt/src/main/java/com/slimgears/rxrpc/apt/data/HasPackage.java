/**
 *
 */
package com.slimgears.rxrpc.apt.data;

public interface HasPackage {
    String packageName();

    interface Builder<B extends Builder<B>> {
        B packageName(String name);
    }
}
