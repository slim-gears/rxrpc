package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;

import javax.lang.model.element.VariableElement;

@AutoValue
public abstract class ParamInfo implements HasName, HasType {
    public static ParamInfo of(VariableElement element) {
        return create(element.getSimpleName().toString(), TypeInfo.parse(element.asType().toString()));
    }

    public static ParamInfo create(String name, TypeInfo type) {
        return new AutoValue_ParamInfo(name, type);
    }
}
