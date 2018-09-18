package com.slimgears.rxrpc.apt.typescript; /**
 *
 */

import com.slimgears.apt.data.TypeInfo;
import com.slimgears.apt.util.AnnotationProcessingTester;
import com.slimgears.rxrpc.apt.TestBundles;
import com.slimgears.util.generic.ScopedInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;

public class TypeScriptEndpointGenerationTest {
    private ScopedInstance.Closable classTrackerDisposable;

    @Before
    public void setUp() {
        classTrackerDisposable = GeneratedClassTracker.trackFiles();
    }

    @After
    public void tearDown() {
        classTrackerDisposable.close();
    }

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

    @Test
    public void testNestedDataGeneration() {
        TestBundles.sampleNestedDataEndpointTester()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-nested-data-endpoint.ts",
                        "sample-nested-data-endpoint-data.ts",
                        "sample-nested-data-endpoint-data-type.ts",
                        "sample-nested-data-endpoint-client.ts")
                .test();
    }

    @Test
    public void testDerivedAndBaseDataGeneration() {
        TestBundles.sampleDerivedDataEndpointTester()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-base-data.ts",
                        "sample-derived-data.ts")
                .test();
    }

    @Test
    public void testDefaultNameEndpointGeneration() {
        TestBundles.sampleDefaultNameEndpointTester()
                .apply(this::typeScriptOptions)
                .expectedFiles("sample-default-name-endpoint-client.ts")
                .test();
    }

    @Test
    public void testMapDataEndpointGeneration() {
        TestBundles.sampleMapEndpointDataTester()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-map-data.ts",
                        "sample-map-endpoint.ts",
                        "sample-map-endpoint-client.ts")
                .test();
    }

    @Test
    public void testEndpointPointModuleGeneration() {
        TestBundles.sampleEndpointModuleTester()
                .apply(this::typeScriptOptions)
                .expectedFiles("sample-module.ts")
                .test();
    }

    private AnnotationProcessingTester typeScriptOptions(AnnotationProcessingTester tester) {
        return tester
                .verbosity(Level.TRACE)
                .options(
                        "-Arxrpc.ts.ngmodule",
                        "-Arxrpc.ts.npm");
    }
}
