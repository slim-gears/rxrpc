/**
 * Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved
 */
package com.slimgears.rxrpc.apt;

import org.junit.Test;

public class EndpointGenerationTest {
    @Test
    public void testEndpointGeneration() {
        AnnotationProcessingTester.create()
                .inputFiles("SampleEndpoint.java", "SampleRequest.java")
                .expectedFiles("SampleEndpoint_RxClient.java")
                .processedWith(new RxRpcAnnotationProcessor())
                .test();
    }
}
