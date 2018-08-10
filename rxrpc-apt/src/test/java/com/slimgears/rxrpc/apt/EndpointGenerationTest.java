/**
 *
 */
package com.slimgears.rxrpc.apt;

import org.junit.Test;

public class EndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleEndpoint.java", "SampleRequest.java", "SampleData.java", "SampleEnum.java")
                .expectedSources("SampleEndpoint_RxClient.java", "SampleEndpoint_RxModule.java")
                .processedWith(new RxRpcEndpointAnnotationProcessor())
                .test();
    }

    @Test
    public void testDataClassGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleRequest.java", "SampleData.java", "SampleEnum.java")
                .options("-AtsOutDir=")
                .expectedFiles("sample-enum.ts", "sample-request.ts", "sample-data.ts")
                .processedWith(new RxRpcDataAnnotationProcessor())
                .test();
    }

    @Test
    public void testEnumGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleEnum.java")
                .options("-AtsOutDir=")
                .expectedFiles("sample-enum.ts")
                .processedWith(new RxRpcDataAnnotationProcessor())
                .test();
    }
}
