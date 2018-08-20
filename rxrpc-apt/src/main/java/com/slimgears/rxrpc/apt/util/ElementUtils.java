/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.slimgears.rxrpc.apt.data.TypeInfo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.slimgears.rxrpc.apt.util.StreamUtils.ofType;
import static com.slimgears.rxrpc.apt.util.StreamUtils.self;

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
            .add(types(Optional.class))
            .add("io.reactivex.Observable")
            .add("io.reactivex.Single")
            .add("io.reactivex.Maybe")
            .add("io.reactivex.Completable")
            .build();

    private final static ThreadLocal<ProcessingEnvironment> processingEnvironment = new ThreadLocal<>();

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

    public static boolean isInterface(Element element) {
        return isOfKind(element, ElementKind.INTERFACE);
    }

    public static boolean isOfKind(Element element, ElementKind kind) {
        return element.getKind() == kind;
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

    public static boolean hasAnnotation(Element elemenet, Class<? extends Annotation> annotationCls) {
        return elemenet.getAnnotation(annotationCls) != null;
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
                                .flatMap(ElementUtils::toTypeElement)))
                .filter(ElementUtils::isUnknownType)
                .distinct();
    }

    public static Stream<TypeElement> getReferencedTypes(ExecutableElement executableElement) {
        return Stream.concat(
                Stream.of(executableElement.getReturnType()),
                executableElement.getParameters().stream()
                        .map(VariableElement::asType))
                        .flatMap(ElementUtils::getReferencedTypeParams)
                .flatMap(ElementUtils::toTypeElement)
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
                .flatMap(self())
                .distinct();
    }

    public static Stream<DeclaredType> getHierarchy(DeclaredType declaredType) {
        return Stream.of(
                Stream.of(declaredType),
                getSuperClass(declaredType),
                getInterfaces(declaredType))
                .flatMap(self())
                .distinct();
    }

    public static Stream<ExecutableElement> getMethods(DeclaredType declaredType) {
        return toTypeElement(declaredType)
                .map(TypeElement::getEnclosedElements)
                .flatMap(Collection::stream)
                .flatMap(ofType(ExecutableElement.class))
                .filter(ElementUtils::isPublic)
                .filter(ElementUtils::isNotStatic);
    }

    public static Stream<TypeElement> getHierarchy(TypeElement typeElement) {
        return Stream.of(
                Stream.of(typeElement),
                Stream.of(typeElement)
                        .map(TypeElement::getSuperclass)
                        .flatMap(ElementUtils::toTypeElement)
                        .flatMap(ElementUtils::getHierarchy),
                Stream.of(typeElement)
                        .flatMap(t -> t.getInterfaces().stream())
                        .flatMap(ElementUtils::toTypeElement)
                        .flatMap(ElementUtils::getHierarchy))
                .flatMap(self())
                .distinct();
    }

    public static Stream<DeclaredType> toDeclaredTypeStream(TypeElement typeElement) {
        return Stream.of(typeElement)
                .map(TypeElement::asType)
                .flatMap(ofType(DeclaredType.class));
    }

    public static DeclaredType toDeclaredType(TypeElement typeElement) {
        return toDeclaredTypeStream(typeElement)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot convert " + typeElement.getQualifiedName() + " to DeclaredType"));
    }

    public static Stream<TypeElement> toTypeElement(TypeMirror type) {
        return Stream.of(type)
                .flatMap(ofType(DeclaredType.class))
                .map(DeclaredType::asElement)
                .flatMap(ofType(TypeElement.class));
    }

    public static <A extends Annotation> TypeInfo[] typesFromAnnotation(A annotation, Function<A, Class[]> classRetriever) {
        try {
            return Stream.of(classRetriever.apply(annotation)).map(TypeInfo::of).toArray(TypeInfo[]::new);
        } catch (MirroredTypesException e) {
            return e.getTypeMirrors().stream().map(TypeInfo::of).toArray(TypeInfo[]::new);
        }
    }

    public static <A extends Annotation> TypeInfo typeFromAnnotation(A annotation, Function<A, Class> classRetriever) {
        try {
            return TypeInfo.of(classRetriever.apply(annotation));
        } catch (MirroredTypeException e) {
            return TypeInfo.of(e.getTypeMirror());
        }
    }

    private static Stream<DeclaredType> getSuperClass(DeclaredType type) {
        return toTypeElement(type)
                .map(TypeElement::getSuperclass)
                .flatMap(ofType(DeclaredType.class));
    }

    private static Stream<DeclaredType> getInterfaces(DeclaredType type) {
        return toTypeElement(type)
                .flatMap(e -> e.getInterfaces().stream())
                .flatMap(ofType(DeclaredType.class));
    }
}
