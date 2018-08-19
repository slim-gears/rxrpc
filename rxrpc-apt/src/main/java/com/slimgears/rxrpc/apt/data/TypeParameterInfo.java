package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeVariable;

@AutoValue
public abstract class TypeParameterInfo implements HasName, HasType {
    public static TypeParameterInfo.Builder builder() {
        return new AutoValue_TypeParameterInfo.Builder();
    }

    public static TypeParameterInfo of(TypeParameterElement element) {
        return of(element.getSimpleName().toString(), TypeInfo.of(element.asType()));
    }

    public static TypeParameterInfo of(TypeVariable typeVariable) {
        return of(typeVariable.asElement().getSimpleName().toString(), TypeInfo.of(typeVariable.asElement().asType()));
    }

    public static TypeParameterInfo of(String name, TypeInfo type) {
        return builder().name(name).type(type).build();
    }

    @AutoValue.Builder
    public interface Builder extends InfoBuilder<TypeParameterInfo>, HasName.Builder<Builder>, HasType.Builder<Builder> {
    }
}
