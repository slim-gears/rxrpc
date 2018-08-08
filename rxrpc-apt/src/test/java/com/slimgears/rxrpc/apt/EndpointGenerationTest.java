/**
 *
 */
package com.slimgears.rxrpc.apt;

import org.junit.Test;

public class EndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleEndpoint.java", "SampleRequest.java")
                .expectedFiles("SampleEndpoint_RxClient.java", "SampleEndpoint_RxModule.java")
                .processedWith(new RxRpcAnnotationProcessor())
                .test();
    }
}
