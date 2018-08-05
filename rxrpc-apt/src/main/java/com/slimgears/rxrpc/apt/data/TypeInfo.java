/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TypeInfo implements HasName {
    public abstract String name();

    public static TypeInfo of(String name) {
        return new AutoValue_TypeInfo(name);
    }
}
