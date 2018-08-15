/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.slimgears.rxrpc.apt.AnnotationProcessingTester;
import com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor;
import org.junit.Test;
import org.slf4j.event.Level;

public class JavaEndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        AnnotationProcessingTester.create()
                .options(
                        "-Arxrpc.java.client",
                        "-Arxrpc.java.server",
                        "-Arxrpc.java.autoservice")
                .inputFiles(
                        "SampleBaseEndpoint.java",
                        "SampleArrayEndpoint.java",
                        "SampleArray.java",
                        "SampleEndpoint.java",
                        "SampleRequest.java",
                        "SampleData.java",
                        "SampleEnum.java")
                .expectedSources(
                        "SampleEndpoint_RxClient.java",
                        "SampleEndpoint_RxModule.java")
                .processedWith(new RxRpcEndpointAnnotationProcessor())
                .verbosity(Level.TRACE)
                .test();
    }
}
