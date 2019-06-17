package com.slimgears.rxrpc.apt;

import com.slimgears.apt.util.AnnotationProcessingTester;
import org.slf4j.event.Level;

public class TestBundles {
    static AnnotationProcessingTester rxRpcEndpointProcessingTester() {
        return AnnotationProcessingTester.create()
                .processedWith(new RxRpcEndpointAnnotationProcessor())
                .verbosity(Level.INFO);
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
                        "SampleEnum.java",
                        "SampleGenericData.java");
    }

    public static AnnotationProcessingTester sampleSpecializedEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleGenericData.java",
                        "SampleGenericList.java",
                        "SampleGenericEndpoint.java",
                        "SampleSpecializedData.java",
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
                .processedWith(new RxRpcGenerateAnnotationProcessor());
    }

    public static AnnotationProcessingTester sampleGenericMetaEndpointClassTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleGenericMetaEndpointInterface.java",
                        "SampleGenericMetaEndpointClass.java")
                .processedWith(new RxRpcGenerateAnnotationProcessor());
    }

    public static AnnotationProcessingTester sampleMetaDefaultNameEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles("SampleGenericMetaDefaultNameEndpoint.java")
                .processedWith(new RxRpcGenerateAnnotationProcessor());
    }

    public static AnnotationProcessingTester sampleNestedDataEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles("SampleNestedDataEndpoint.java");
    }

    public static AnnotationProcessingTester sampleDerivedDataEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleBaseData.java",
                        "SampleDerivedData.java",
                        "SampleDerivedDataEndpoint.java");
    }

    public static AnnotationProcessingTester sampleDefaultNameEndpointTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles("SampleDefaultNameEndpoint.java");
    }

    public static AnnotationProcessingTester sampleMapEndpointDataTester() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleEnum.java",
                        "SampleData.java",
                        "SampleGenericData.java",
                        "SampleMapData.java",
                        "SampleMapEndpoint.java");
    }

    public static AnnotationProcessingTester sampleEndpointModuleTester() {
        return sampleEndpointTester()
                .inputFiles("SampleEndpointModule.java")
                .processedWith(new RxRpcModuleAnnotationProcessor());
    }

    public static AnnotationProcessingTester sampleCircularReferenceDataEndpoint() {
        return rxRpcEndpointProcessingTester()
                .inputFiles(
                        "SampleCircularReferenceData.java",
                        "SampleCircularReferenceEndpoint.java");
    }
}
