package com.slimgears.rxrpc.apt.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

@AutoValue
public abstract class ClassInfo implements HasName {
    public abstract String packageName();
    public abstract TypeInfo type();
    public abstract ImmutableList<MethodInfo> methods();
    public String name() {
        return type().name();
    }
    public String simpleName() { return type().simpleName(); }

    public static Builder builder() {
        return new AutoValue_ClassInfo.Builder();
    }

    public static ClassInfo of(TypeElement typeElement) {

        Builder builder = builder()
                .packageName(packageName(typeElement.getQualifiedName()))
                .type(TypeInfo.of(typeElement));

        typeElement.getEnclosedElements()
                .stream()
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .map(MethodInfo::of)
                .forEach(builder::method);

        return builder.build();
    }

    @AutoValue.Builder
    public interface Builder {
        Builder packageName(String packageName);
        Builder type(TypeInfo type);
        ImmutableList.Builder<MethodInfo> methodsBuilder();
        ClassInfo build();

        default Builder method(MethodInfo method) {
            methodsBuilder().add(method);
            return this;
        }
    }

    public static String packageName(String qualifiedName) {
        int pos = qualifiedName.lastIndexOf('.');
        return (pos >= 0) ? qualifiedName.substring(0, pos) : "";
    }

    public static String packageName(Name name) {
        return packageName(name.toString());
    }
}
