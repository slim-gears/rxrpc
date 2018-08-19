/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.slimgears.rxrpc.apt.AnnotationProcessingTester;
import com.slimgears.rxrpc.apt.TestBundles;
import org.junit.Test;

public class JavaEndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        TestBundles.sampleEndpointTester()
                .apply(this::javaOptions)
                .options("-Arxrpc.java.autoservice")
                .expectedSources(
                        "SampleEndpoint_RxClient.java",
                        "SampleEndpoint_RxModule.java")
                .test();
    }

    @Test
    public void testSpecializedEndpointGeneration() {
        TestBundles.sampleSpecializedEndpointTester()
                .apply(this::javaOptions)
                .expectedSources(
                        "SampleSpecializedEndpoint_RxClient.java",
                        "SampleSpecializedEndpoint_RxModule.java")
                .test();
    }

    private AnnotationProcessingTester javaOptions(AnnotationProcessingTester tester) {
        return tester.options(
                        "-Arxrpc.java.client",
                        "-Arxrpc.java.server");
    }
}
