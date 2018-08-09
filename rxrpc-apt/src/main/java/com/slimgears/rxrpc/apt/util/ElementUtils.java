/**
 *
 */
package com.slimgears.rxrpc.apt.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Set;
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
}
