/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.TypeInfo;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TypeInfoParser {
    private static final String nameChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ._$0123456789";
    private static final char beginParamsChar = '<';
    private static final char endParamsChar = '>';
    private static final char nextParamChar = ',';
    private static final String whiteSpaceChars = " \t\n\r";

    interface TypeVisitor {
        void visitIdentifier(String name);
        void visitBeginParams();
        void visitEndParams();
        void visitNextParam();
    }

    public static TypeInfo parse(String str) {
        InternalVisitor visitor = new InternalVisitor();
        parse(str, visitor);
        return visitor.getType();
    }

    private static class InternalVisitor implements TypeVisitor {
        private final Stack<TypeInfo.Builder> builders = new Stack<>();
        private final Stack<Integer> paramsCounters = new Stack<>();

        public TypeInfo getType() {
            assert builders.size() == 1;
            return getTypes()[0];
        }

        public TypeInfo[] getTypes() {
            return popTypes(builders.size());
        }

        @Override
        public void visitIdentifier(String name) {
            builders.push(TypeInfo.builder().name(name));
            if (!paramsCounters.isEmpty()) {
                paramsCounters.push(paramsCounters.pop() + 1);
            }
        }

        @Override
        public void visitBeginParams() {
            paramsCounters.push(0);
        }

        @Override
        public void visitEndParams() {
            int paramCount = paramsCounters.pop();
            Stream.of(popTypes(paramCount))
                    .forEach(builders.peek()::typeParam);
        }

        private TypeInfo[] popTypes(int count) {
            List<TypeInfo> params = IntStream
                    .range(0, count)
                    .mapToObj(i -> builders.pop())
                    .map(TypeInfo.Builder::build)
                    .collect(Collectors.toList());

            return IntStream
                    .iterate(count - 1, p -> p - 1)
                    .limit(count)
                    .mapToObj(params::get)
                    .toArray(TypeInfo[]::new);
        }

        @Override
        public void visitNextParam() {

        }
    }

    private static void parse(String str, TypeVisitor visitor) {
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char curChar = str.charAt(i);
            if (isNameChar(curChar)) {
                nameBuilder.append(curChar);
            } else if (nameBuilder.length() > 0) {
                visitor.visitIdentifier(nameBuilder.toString());
                nameBuilder.delete(0, nameBuilder.length());
            }
            if (isBeginParams(curChar)) {
                visitor.visitBeginParams();
            } else if (isEndParams(curChar)) {
                visitor.visitEndParams();
            } else if (isNextParam(curChar)) {
                visitor.visitNextParam();
            }
        }
    }

    private static boolean isNameChar(char c) {
        return nameChars.indexOf(c) >= 0;
    }

    private static boolean isBeginParams(char c) {
        return c == beginParamsChar;
    }

    private static boolean isEndParams(char c) {
        return c == endParamsChar;
    }

    private static boolean isNextParam(char c) {
        return c == nextParamChar;
    }

    private static boolean isWhiteSpace(char c) {
        return whiteSpaceChars.indexOf(c) >= 0;
    }
}
