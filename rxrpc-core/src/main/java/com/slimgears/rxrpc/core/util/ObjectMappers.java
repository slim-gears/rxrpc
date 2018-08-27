/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.slimgears.util.stream.Lazy;

import java.util.function.Supplier;

public class ObjectMappers {
    public static ObjectMapper create() {
        return new ObjectMapper();
    }

    public static Supplier<ObjectMapper> fromSupplier(Supplier<ObjectMapper> supplier) {
        return Lazy.of(() -> supplier.get().copy()
                 .registerModule(new GuavaModule())
                 .registerModule(new Jdk8Module()));
    }
}
