package com.slimgears.rxrpc.apt.typescript; /**
 *
 */

import com.slimgears.rxrpc.apt.AnnotationProcessingTester;
import com.slimgears.rxrpc.apt.TestBundles;
import org.junit.Test;

public class TypeScriptEndpointGenerationTest {
    @Test
    public void testEndpointClientGeneration() {
        TestBundles.sampleEndpointTester()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-array-endpoint.ts",
                        "sample-base-endpoint.ts",
                        "sample-endpoint.ts",
                        "sample-endpoint-client.ts",
                        "sample-request.ts",
                        "sample-data.ts",
                        "sample-enum.ts",
                        "sample-array.ts",
                        "index.ts",
                        "module.ts",
                        "tsconfig.json")
                .test();
    }

    @Test
    public void testSpecializedClientGeneration() {
        TestBundles.sampleSpecializedEndpointTester()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-generic-data.ts",
                        "sample-generic-endpoint.ts",
                        "sample-specialized-endpoint.ts",
                        "sample-specialized-endpoint-client.ts")
                .test();
    }

    @Test
    public void testOptionalDataGeneration() {
        TestBundles.sampleOptionalDataTester()
                .apply(this::typeScriptOptions)
                .expectedFiles("sample-optional-data.ts")
                .test();
    }

    private AnnotationProcessingTester typeScriptOptions(AnnotationProcessingTester tester) {
        return tester.options(
                        "-Arxrpc.ts.ngmodule",
                        "-Arxrpc.ts.npm");
    }
}
