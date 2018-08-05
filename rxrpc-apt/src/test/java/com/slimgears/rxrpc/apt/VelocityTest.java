package com.slimgears.rxrpc.apt;

import com.google.common.collect.ImmutableMap;
import com.google.escapevelocity.Template;
import com.slimgears.rxrpc.apt.data.ClassInfo;
import com.slimgears.rxrpc.apt.data.MethodInfo;
import com.slimgears.rxrpc.apt.data.ParamInfo;
import com.slimgears.rxrpc.apt.data.TypeInfo;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class VelocityTest {
    private static final String template = "Foo value: $foo, Bar value: $bar";

    @Test
    public void testSimpleTemplate() throws IOException {
        ImmutableMap<String, Object> vars = ImmutableMap.<String, Object>builder()
                .put("foo", "fooVal")
                .put("bar", "barVal")
                .build();
        Template template = Template.parseFrom(new StringReader(VelocityTest.template));
        String result = template.evaluate(vars);
        Assert.assertEquals("Foo value: fooVal, Bar value: barVal", result);
    }

    @Test
    public void testTypeScriptTemplate() {
        String typeScript = TemplateEvaluator
                .forResource("/TypeScriptClient.ts.vm")
                .variable("cls", ClassInfo
                        .builder()
                        .name("DummyEndpoint")
                        .addMethod(MethodInfo.builder()
                                .name("echoMethod")
                                .addParam("msg", TypeInfo.of("string"))
                                .returnType(TypeInfo.of("Observable<string>"))
                                .builder())
                        .build())
                .evaluate();
        System.out.println(typeScript);
    }
}
