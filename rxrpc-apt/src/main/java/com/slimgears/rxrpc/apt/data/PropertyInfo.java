/**
 *
 */
package com.slimgears.rxrpc.apt.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.slimgears.rxrpc.apt.util.ElementUtils;
import com.slimgears.rxrpc.apt.util.Optionals;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

@AutoValue
public abstract class PropertyInfo implements HasName, HasType {
    public static Builder builder() {
        return new AutoValue_PropertyInfo.Builder();
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

    public static boolean isProperty(Element element) {
        return of(element).isPresent();
    }

    public static Optional<PropertyInfo> of(Element element) {
        return Optionals.or(
                () -> fromPropertyGetter(element),
                () -> fromPublicField(element));
    }

    private static Optional<PropertyInfo> fromPropertyGetter(Element element) {
        return Optional
                .of(element)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(ElementUtils::isPublic)
                .filter(ElementUtils::isNotStatic)
                .filter(el -> el.getParameters().size() == 0)
                .filter(el -> el.getReturnType().getKind() != TypeKind.VOID)
                .map(el -> fromJsonProperty(el, el.getReturnType())
                        .orElseGet(() -> of(propertyName(el), el.getReturnType())));
    }

    private static Optional<PropertyInfo> fromPublicField(Element element) {
        return Optional.of(element)
                .filter(VariableElement.class::isInstance)
                .filter(ElementUtils::isPublic)
                .filter(ElementUtils::isNotStatic)
                .map(el -> fromJsonProperty(el, el.asType())
                        .orElseGet(() -> of(el.getSimpleName().toString(), element.asType())));
    }

    private static Optional<PropertyInfo> fromJsonProperty(Element element, TypeMirror type) {
        return Optional.ofNullable(element.getAnnotation(JsonProperty.class))
                .map(JsonProperty::value)
                .filter(n -> !n.isEmpty())
                .map(name -> of(name, type));
    }

    private static String propertyName(ExecutableElement element) {
        String name = element.getSimpleName().toString();
        if (startsWith(name, "is") && TypeInfo.of(element.getReturnType()).isOneOf(boolean.class, Boolean.class)) {
            name = name.substring(2);
        } else if (startsWith(name, "get")) {
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
    }
}
