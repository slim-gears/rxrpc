/**
 *
 */
package com.google.auto.value.processor;

public class Utils {
    public static String reformat(String code) {
        return Reformatter.fixup(code);
    }
}
