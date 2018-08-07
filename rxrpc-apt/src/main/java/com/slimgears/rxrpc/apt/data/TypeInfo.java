package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.slimgears.rxrpc.apt.TypeInfoParser;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoValue
public abstract class TypeInfo implements HasName {
    private final static ImmutableMap<TypeInfo, TypeInfo> boxableTypes = ImmutableMap.<TypeInfo, TypeInfo>builder()
            .put(TypeInfo.of("boolean"), TypeInfo.of("Boolean"))
            .put(TypeInfo.of("short"), TypeInfo.of("Short"))
            .put(TypeInfo.of("int"), TypeInfo.of("Integer"))
            .put(TypeInfo.of("long"), TypeInfo.of("Long"))
            .put(TypeInfo.of("float"), TypeInfo.of("Float"))
            .put(TypeInfo.of("double"), TypeInfo.of("Double"))
            .put(TypeInfo.of("char"), TypeInfo.of("Char"))
            .build();

    public abstract String name();
    public abstract ImmutableList<TypeInfo> typeParams();

    public String fullName() {
        return (typeParams().isEmpty())
                ? name()
                : name() + typeParams()
                .stream()
                .map(TypeInfo::fullName)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public TypeInfo elementType() {
        return typeParams().isEmpty()
                ? this
                : typeParams().get(0);
    }

    public String simpleName() {
        return name().substring(name().lastIndexOf('.') + 1);
    }

    public String packageName() {
        int lastDotIndex = name().lastIndexOf('.');
        return lastDotIndex >= 0
                ? name().substring(0, lastDotIndex)
                : "";
    }

    public boolean is(String name) {
        return name().equals(name);
    }

    public static Builder builder() {
        return new AutoValue_TypeInfo.Builder();
    }

    public static TypeInfo of(String name, TypeInfo... params) {
        return builder().name(name).typeParams(params).build();
    }

    public static TypeInfo parse(String fullName) {
        return TypeInfoParser.parse(fullName);
    }

    public static TypeInfo of(TypeMirror typeMirror) {
        return parse(typeMirror.toString());
    }

    public static TypeInfo of(TypeElement typeElement) {
        Builder builder = builder().name(typeElement.getQualifiedName().toString());
        typeElement.getTypeParameters()
                .stream()
                .map(Element::asType)
                .map(TypeInfo::of)
                .forEach(builder::typeParam);
        return builder.build();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder name(String name);
        ImmutableList.Builder<TypeInfo> typeParamsBuilder();
        TypeInfo build();

        default Builder typeParam(TypeInfo param) {
            typeParamsBuilder().add(param);
            return this;
        }

        default Builder typeParams(TypeInfo... params) {
            Arrays.asList(params).forEach(this::typeParam);
            return this;
        }
    }

    public TypeInfo asBoxed() {
        return Optional.ofNullable(boxableTypes.get(this)).orElse(this);
    }

    @Override
    public String toString() {
        return fullName();
    }
}
