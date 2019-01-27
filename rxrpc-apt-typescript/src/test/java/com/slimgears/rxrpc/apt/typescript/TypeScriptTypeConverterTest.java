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

@RunWith(MockitoJUnitRunner.class)
public class TypeScriptTypeConverterTest {
    private @Mock ProcessingEnvironment processingEnvironment;
    private @Mock RoundEnvironment roundEnvironment;

    @Test
    public void testGenericArrayConversion() {
        try (Safe.Closable ignored = Environment.withEnvironment(processingEnvironment, roundEnvironment)) {
            TypeInfo sourceType = TypeInfo.arrayOf(TypeInfo.builder().name("Item").typeParam("T", TypeInfo.of("T")).build());
            TypeInfo tsType = TypeScriptUtils
                    .create()
                    .toTypeScriptType(sourceType);
            System.out.println(sourceType.name());
            System.out.println(tsType.name());
            Assert.assertEquals("Item<T>[]", tsType.toString());
        }
    }
}
