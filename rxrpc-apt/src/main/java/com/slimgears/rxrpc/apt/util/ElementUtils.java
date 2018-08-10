/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ElementUtils {
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
}
