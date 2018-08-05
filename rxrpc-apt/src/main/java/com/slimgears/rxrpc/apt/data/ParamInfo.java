/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ParamInfo implements HasName {
    public abstract String name();
    public abstract TypeInfo type();

    public static ParamInfo create(String name, TypeInfo type) {
        return new AutoValue_ParamInfo(name, type);
    }
}
