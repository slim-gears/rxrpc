package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.TypeInfo;
import org.junit.Assert;
import org.junit.Test;


public class TypeInfoParserTest {
    @Test
    public void testTypeInfoParser() {
        TypeInfo typeInfo = TypeInfoParser.parse("java.util.List<java.util.Map<java.lang.String, java.util.List<java.lang.String>>>");
        Assert.assertEquals("java.util.List", typeInfo.name());
        Assert.assertEquals("List", typeInfo.simpleName());
        Assert.assertEquals("java.util", typeInfo.packageName());
        Assert.assertEquals("java.util.List<java.util.Map<java.lang.String, java.util.List<java.lang.String>>>", typeInfo.fullName());
    }

    @Test
    public void testImportTracker() {
        ImportTracker importTracker = new ImportTracker();
        String simplified = importTracker.useClass("java.util.List<java.util.Map<java.lang.String, java.util.List<java.lang.String>>>");
        Assert.assertEquals("List<Map<String, List<String>>>", simplified);
        Assert.assertEquals(3, importTracker.imports().length);
    }
}