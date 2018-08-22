/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.internal.PropertyModules;
import com.slimgears.rxrpc.apt.internal.TypeConversionModule;
import com.slimgears.rxrpc.apt.util.ConfigProviders;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;
import java.util.Set;

public class PropertyInjectionTest {
    @Inject @Named("strValue") String strVal;
    @Inject @Named("intValue") int intVal;
    @Inject @Named("strArrayValue") String[] strArrVal;
    @Inject @Named("intArrayValue") int[] intArrVal;
    @Inject @Named("typeArray") TypeInfo[] typeArray;
    @Inject @Named("typeSet") Set<TypeInfo> typeSet;

    @Test
    public void testIntInjection() {
        Properties properties = ConfigProviders.create(
                p -> p.put("strValue", "Hello"),
                p -> p.put("intValue", "2"),
                p -> p.put("strArrayValue", "One, Two, Three"),
                p -> p.put("intArrayValue", "1, 2, 3 , 4"),
                p -> p.put("typeArray", "java.lang.String , java.lang.Integer"),
                p -> p.put("typeSet", "java.lang.Float , java.lang.Double"));

        Injector injector = Guice.createInjector(new TypeConversionModule(), PropertyModules.forProperties(properties));
        injector.injectMembers(this);

        Assert.assertEquals("Hello", strVal);
        Assert.assertEquals(2, intVal);
        Assert.assertEquals(3, strArrVal.length);
        Assert.assertEquals("Two", strArrVal[1]);
        Assert.assertEquals(4, intArrVal.length);
        Assert.assertEquals(3, intArrVal[2]);
        Assert.assertEquals(2, typeArray.length);
        Assert.assertEquals(TypeInfo.of(String.class), typeArray[0]);
        Assert.assertEquals(2, typeSet.size());
        Assert.assertTrue(typeSet.contains(TypeInfo.of(Double.class)));
    }
}
