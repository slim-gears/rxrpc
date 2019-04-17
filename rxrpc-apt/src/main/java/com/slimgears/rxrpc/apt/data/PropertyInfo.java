package com.slimgears.rxrpc.apt.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.HasName;
import com.slimgears.apt.data.HasType;
import com.slimgears.apt.data.InfoBuilder;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.ElementUtils;
import com.slimgears.util.stream.Optionals;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@AutoValue
public abstract class PropertyInfo implements HasName, HasType {
    public abstract boolean isOptional();

    public static Builder builder() {
        return new AutoValue_PropertyInfo.Builder().isOptional(false);
    }

    public static PropertyInfo of(String name, TypeInfo type) {
        return builder().name(name).type(type).build();
    }

    public static PropertyInfo of(String name, TypeElement type) {
        return builder().name(name).type(type).build();
    }

    public static PropertyInfo of(String name, TypeMirror type) {
        return builder().name(name).type(type).build();
    }

    public static Optional<PropertyInfo> fromElement(Element element) {
        return fromElement(MoreTypes.asDeclared(element.getEnclosingElement().asType()), element);
    }

    public static Optional<PropertyInfo> fromElement(DeclaredType owningType, Element element) {
        return Optionals
                .or(
                        () -> fromPropertyGetter(owningType, element),
                        () -> fromPublicField(owningType, element));
    }

    private static Optional<PropertyInfo> fromPropertyGetter(DeclaredType owningType, Element element) {
        return Optional
                .of(element)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(ElementUtils::isPublic)
                .filter(ElementUtils::isNotStatic)
                .filter(el -> el.getParameters().size() == 0)
                .filter(el -> el.getReturnType().getKind() != TypeKind.VOID)
                .map(el -> fromJsonProperty(owningType, el, type -> MoreTypes.asExecutable(type).getReturnType())
                        .orElseGet(() -> builder()
                                .name(propertyName(el))
                                .type(MoreTypes.asExecutable(Environment.instance()
                                        .types()
                                        .asMemberOf(owningType, el))
                                        .getReturnType()))
                        .isOptional(isOptional(el, el.getReturnType()))
                        .build());
    }

    private static Optional<PropertyInfo> fromPublicField(DeclaredType owningType, Element element) {
        return Optional.of(element)
                .filter(VariableElement.class::isInstance)
                .filter(ElementUtils::isPublic)
                .filter(ElementUtils::isNotStatic)
                .map(el -> fromJsonProperty(owningType, el, type -> type)
                        .orElseGet(() -> builder().name(el.getSimpleName().toString()).type(element.asType()))
                        .isOptional(isOptional(el, element.asType()))
                        .build());
    }

    private static boolean isOptional(Element element, TypeMirror propertyType) {
        return ElementUtils.hasAnnotation(element, Nullable.class) ||
                Stream.of(propertyType)
                        .flatMap(ElementUtils::toTypeElement)
                        .anyMatch(te -> te.getQualifiedName().toString().equals(Optional.class.getName()));
    }

    private static Optional<PropertyInfo.Builder> fromJsonProperty(DeclaredType owningType, Element element, Function<TypeMirror, TypeMirror> typeGetter) {
        TypeMirror member = Environment.instance().types().asMemberOf(owningType, element);
        TypeMirror propertyType = typeGetter.apply(member);
        return Optional.ofNullable(element.getAnnotation(JsonProperty.class))
                .map(JsonProperty::value)
                .filter(n -> !n.isEmpty())
                .map(name -> builder().name(name).type(propertyType));
    }


    private static String propertyName(ExecutableElement element) {
        String name = element.getSimpleName().toString();
        if (startsWith(name, "get")) {
            name = name.substring(3);
        }

        if (Character.isUpperCase(name.charAt(0))) {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }

        return name;
    }

    private static boolean startsWith(String name, String prefix) {
        return name.startsWith(prefix) && name.length() > prefix.length() && Character.isUpperCase(name.charAt(prefix.length()));
    }

    @AutoValue.Builder
    public interface Builder extends InfoBuilder<PropertyInfo>, HasName.Builder<Builder>, HasType.Builder<Builder> {
        Builder isOptional(boolean optional);
    }
}
