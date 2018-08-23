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
                        "SampleGenericData.java",
                        "SampleGenericEndpoint.java",
                        "SampleSpecializedEndpoint.java");
    }

    public static AnnotationProcessingTester sampleOptionalDataTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleEnum.java",
                        "SampleOptionalData.java",
                        "SampleOptionalDataEndpoint.java");
    }

    public static AnnotationProcessingTester sampleGenericMetaEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleGenericMetaEndpoint.java",
                        "SampleGenericData.java")
                .processedWith(new RxRpcGenerateAnnotationProcessor());
    }

    public static AnnotationProcessingTester sampleMetaEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleMetaEndpoint.java",
                        "SampleMetaEndpointInput.java")
                .processedWith(
                        new RxRpcGenerateAnnotationProcessor(),
                        new RxRpcEndpointAnnotationProcessor());
    }

    public static AnnotationProcessingTester sampleNestedDataEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles("SampleNestedDataEndpoint.java")
                .processedWith(new RxRpcEndpointAnnotationProcessor());
    }
}
