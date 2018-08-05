/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class ClassInfo implements HasName {
    public abstract String name();
    public abstract ImmutableList<MethodInfo> methods();

    public static Builder builder() {
        return new AutoValue_ClassInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder name(String name);
        ImmutableList.Builder<MethodInfo> methodsBuilder();
        ClassInfo build();

        default Builder addMethod(MethodInfo method) {
            methodsBuilder().add(method);
            return this;
        }
    }
}
