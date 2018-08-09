package com.slimgears.rxrpc.apt.data;

public interface HasName {
    String name();

    interface Builder<B extends Builder<B>> {
        B name(String name);
    }
}
