/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class MethodInfo implements HasName {
    public abstract String name();
    public abstract ImmutableList<ParamInfo> params();
    public abstract TypeInfo returnType();

    public static Builder builder() {
        return new AutoValue_MethodInfo.Builder();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder name(String name);
        ImmutableList.Builder<ParamInfo> paramsBuilder();
        Builder returnType(TypeInfo type);
        MethodInfo builder();

        default Builder addParam(ParamInfo param) {
            paramsBuilder().add(param);
            return this;
        }

        default Builder addParam(String name, TypeInfo type) {
            return addParam(ParamInfo.create(name, type));
        }
    }
}
