/**
 *
 */
package com.slimgears.rxrpc.apt;

import org.junit.Test;

public class EndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleEndpoint.java", "SampleRequest.java", "SampleData.java")
                .expectedFiles("SampleEndpoint_RxClient.java", "SampleEndpoint_RxModule.java")
                .processedWith(new RxRpcEndpointAnnotationProcessor())
                .test();
    }

    @Test
    public void testDataClassGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleRequest.java", "SampleData.java")
                .options("-AtsOutDir=")
                .expectedFiles("sample-request.ts", "sample-data.ts")
                .processedWith(new RxRpcDataAnnotationProcessor())
                .test();
    }
}
