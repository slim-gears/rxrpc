/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.util.TemplateUtils;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {
    @Test
    public void testCamelCaseToDash() {
        Assert.assertEquals("sample-request", TemplateUtils.instance.camelCaseToDash("SampleRequest"));
    }
}
