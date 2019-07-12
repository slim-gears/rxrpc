package com.slimgears.rxrpc.apt.typescript;

import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.util.stream.Safe;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.time.Instant;
import java.time.LocalDateTime;

@RunWith(MockitoJUnitRunner.class)
public class TypeScriptTypeConverterTest {
    private @Mock ProcessingEnvironment processingEnvironment;
    private @Mock RoundEnvironment roundEnvironment;

    @Test
    public void testGenericArrayConversion() {
        try (Safe.Closeable ignored = Environment.withEnvironment(processingEnvironment, roundEnvironment)) {
            TypeInfo sourceType = TypeInfo.arrayOf(TypeInfo.builder().name("Item").typeParam("T", TypeInfo.of("T")).build());
            TypeInfo tsType = TypeScriptUtils
                    .create()
                    .toTypeScriptType(sourceType);
            System.out.println(sourceType.name());
            System.out.println(tsType.name());
            Assert.assertEquals("Item<T>[]", tsType.toString());
        }
    }

    @Test
    public void testGenericListConversion() {
        try (Safe.Closeable ignored = Environment.withEnvironment(processingEnvironment, roundEnvironment)) {
            TypeInfo sourceType = TypeInfo.of("com.google.common.collect.ImmutableList<Item<T>>");
            TypeInfo tsType = TypeScriptUtils
                    .create()
                    .toTypeScriptType(sourceType);
            System.out.println(sourceType.name());
            System.out.println(tsType.name());
            Assert.assertEquals("Item<T>[]", tsType.toString());
        }
    }

    @Test
    public void testNestedClassCoversionToTypeScript() {
        TypeInfo nestedType = TypeInfo.of("com.slimgears.rxrpc.apt.typescript.TypeScriptTypeConverterTest$Nested$Nested2");
        try (Safe.Closeable ignored = Environment.withEnvironment(processingEnvironment, roundEnvironment)) {
            TypeInfo tsType = TypeScriptUtils
                    .create()
                    .toTypeScriptType(nestedType);
            System.out.println(nestedType);
            System.out.println(tsType.name());
            Assert.assertEquals(nestedType.nameWithoutPackage().replace("$", ""), tsType.toString());
        }
    }
}
