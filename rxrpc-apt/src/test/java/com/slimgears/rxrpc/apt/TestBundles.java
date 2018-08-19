package com.slimgears.rxrpc.apt;

import org.slf4j.event.Level;

public class TestBundles {
    static AnnotationProcessingTester rxRpcEndpointProcessingTester() {
        return AnnotationProcessingTester.create()
                .processedWith(new RxRpcEndpointAnnotationProcessor())
                .verbosity(Level.TRACE);
    }

    public static AnnotationProcessingTester sampleEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleBaseEndpoint.java",
                        "SampleArrayEndpoint.java",
                        "SampleArray.java",
                        "SampleEndpoint.java",
                        "SampleRequest.java",
                        "SampleData.java",
                        "SampleEnum.java");
    }

    public static AnnotationProcessingTester sampleSpecializedEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleGenericEndpoint.java",
                        "SampleSpecializedEndpoint.java");
    }
}
