/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementUtils {
    private final static ImmutableSet<String> knownClasses = ImmutableSet
            .<String>builder()
            .add(types(byte.class, Byte.class))
            .add(types(short.class, Short.class))
            .add(types(int.class, Integer.class))
            .add(types(long.class, Long.class))
            .add(types(float.class, Float.class))
            .add(types(double.class, Double.class))
            .add(types(char.class, Character.class, String.class))
            .add(types(BigInteger.class, BigDecimal.class))
            .add(types(Map.class, List.class))
            .add(types(Future.class))
            .add(types(JsonNode.class))
            .add(types(Object.class))
            .add("io.reactivex.Observable")
            .add("io.reactivex.Single")
            .add("io.reactivex.Maybe")
            .add("io.reactivex.Completable")
            .build();

    private static String[] types(Class... classes) {
        return Stream.of(classes).map(Class::getName).toArray(String[]::new);
    }

    public static boolean isKnownType(TypeElement typeElement) {
        return knownClasses.contains(typeElement.getQualifiedName().toString());
    }

    public static boolean isUnknownType(TypeElement typeElement) {
        return !knownClasses.contains(typeElement.getQualifiedName().toString());
    }

    public static boolean isKnownType(TypeMirror typeMirror) {
        return knownClasses.contains(typeMirror.toString());
    }

    public static boolean isPublic(Element element) {
        return modifiersContainAll(element, Modifier.PUBLIC);
    }

    public static boolean isNotStatic(Element element) {
        return modifiersContainNone(element, Modifier.STATIC);
    }

    public static boolean modifiersContainAll(Element element, Modifier... modifiers) {
        Set<Modifier> elementModifiers = element.getModifiers();
        return elementModifiers.containsAll(Arrays.asList(modifiers));
    }

    public static boolean modifiersContainNone(Element element, Modifier... modifiers) {
        Set<Modifier> elementModifiers = element.getModifiers();
        return Stream.of(modifiers).noneMatch(elementModifiers::contains);
    }

    public static Predicate<? super Element> ofKind(ElementKind kind) {
        return el -> el.getKind() == kind;
    }

    public static boolean isEnum(TypeElement typeElement) {
        return ofKind(ElementKind.ENUM).test(typeElement);
    }

    public static boolean isEnumConstant(Element element) {
        return ofKind(ElementKind.ENUM_CONSTANT).test(element);
    }

    public static Stream<TypeElement> getReferencedTypes(TypeElement typeElement) {
        return typeElement
                .getEnclosedElements()
                .stream()
                .filter(ElementUtils::isPublic)
                .filter(ElementUtils::isNotStatic)
                .flatMap(element -> Stream.concat(
                        Stream.of(element)
                                .filter(ExecutableElement.class::isInstance)
                                .map(ExecutableElement.class::cast)
                                .flatMap(ElementUtils::getReferencedTypes),
                        Stream.of(element)
                                .filter(VariableElement.class::isInstance)
                                .map(VariableElement.class::cast)
                                .flatMap(v -> getReferencedTypeParams(v.asType()))
                                .filter(DeclaredType.class::isInstance)
                                .map(DeclaredType.class::cast)
                                .map(DeclaredType::asElement)
                                .filter(TypeElement.class::isInstance)
                                .map(TypeElement.class::cast)))
                .filter(ElementUtils::isUnknownType)
                .distinct();
    }

    public static Stream<TypeElement> getReferencedTypes(ExecutableElement executableElement) {
        return Stream.concat(
                Stream.of(executableElement.getReturnType()),
                executableElement.getParameters().stream()
                        .map(VariableElement::asType))
                        .flatMap(ElementUtils::getReferencedTypeParams)
                .filter(DeclaredType.class::isInstance)
                .map(DeclaredType.class::cast)
                .map(DeclaredType::asElement)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .distinct();
    }

    public static Stream<TypeMirror> getReferencedTypeParams(TypeMirror type) {
        return Stream.of(
                Stream.of(type),
                Stream.of(type)
                        .filter(DeclaredType.class::isInstance)
                        .map(DeclaredType.class::cast)
                        .flatMap(t -> t.getTypeArguments().stream())
                        .flatMap(ElementUtils::getReferencedTypeParams),
                Stream.of(type)
                        .filter(ArrayType.class::isInstance)
                        .map(ArrayType.class::cast)
                        .map(ArrayType::getComponentType))
                .flatMap(s -> s)
                .distinct();
    }

    public static Stream<TypeElement> getHierarchy(TypeElement typeElement) {
        return Stream.of(
                Stream.of(typeElement),
                Stream.of(typeElement)
                        .map(TypeElement::getSuperclass)
                        .filter(DeclaredType.class::isInstance)
                        .map(DeclaredType.class::cast)
                        .map(DeclaredType::asElement)
                        .filter(TypeElement.class::isInstance)
                        .map(TypeElement.class::cast)
                        .flatMap(ElementUtils::getHierarchy),
                Stream.of(typeElement)
                        .flatMap(t -> t.getInterfaces().stream())
                        .filter(DeclaredType.class::isInstance)
                        .map(DeclaredType.class::cast)
                        .map(DeclaredType::asElement)
                        .filter(TypeElement.class::isInstance)
                        .map(TypeElement.class::cast)
                        .flatMap(ElementUtils::getHierarchy))
                .flatMap(s -> s)
                .distinct();
    }
}
