package com.slimgears.rxrpc.apt.typescript;

import com.slimgears.apt.data.Environment;
import com.slimgears.apt.data.TypeInfo;
import com.slimgears.util.generic.MoreStrings;
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
        assertConversion(
                TypeInfo.arrayOf(TypeInfo.builder().name("Item").typeParam("T", TypeInfo.of("T")).build()),
                "Item<T>[]");
    }

    @Test
    public void testGenericListConversion() {
        assertConversion("com.google.common.collect.ImmutableList<Item<T>>", "Item<T>[]");
    }

    @Test
    public void testNestedClassCoversionToTypeScript() {
        TypeInfo nestedType = TypeInfo.of("com.slimgears.rxrpc.apt.typescript.TypeScriptTypeConverterTest$Nested$Nested2");
        assertConversion(nestedType, nestedType.nameWithoutPackage().replace("$", ""));
    }

    @Test
    public void testMapConversion() {
        assertConversion("java.util.Map<java.lang.String, com.slimgears.rxrpc.sample.SampleData>",
                "rxrpcJs.StringKeyMap<SampleData>");
        assertConversion("java.util.Map<java.lang.Float, com.slimgears.rxrpc.sample.SampleData>",
                "rxrpcJs.NumberKeyMap<SampleData>");
    }

    private void assertConversion(TypeInfo source, TypeInfo expected) {
        assertConversion(source, expected.toString());
    }

    private void assertConversion(String source, String expected) {
        assertConversion(TypeInfo.of(source), expected);
    }

    private void assertConversion(TypeInfo source, String expected) {
        try (Safe.Closeable ignored = Environment.withEnvironment(processingEnvironment, roundEnvironment)) {
            TypeInfo tsType = TypeScriptUtils
                    .create()
                    .toTypeScriptType(source);
            System.out.println(MoreStrings.format("Converted type {} -> {}", source, tsType));
            Assert.assertEquals(expected, tsType.toString());
        }
    }
}
