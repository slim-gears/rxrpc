package com.slimgears.rxrpc.apt.typescript;

import com.slimgears.apt.util.AnnotationProcessingTester;
import com.slimgears.apt.util.StoreWrittenFilesRule;
import com.slimgears.rxrpc.apt.TestBundles;
import com.slimgears.util.generic.ScopedInstance;
import com.slimgears.util.test.logging.LogLevel;
import com.slimgears.util.test.logging.UseLogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.event.Level;

public class TypeScriptEndpointGenerationTest {
    private ScopedInstance.Closeable classTrackerDisposable;

    @ClassRule
    public final static StoreWrittenFilesRule storeWrittenFilesRule = StoreWrittenFilesRule
            .forPath("build/test-results/files");

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
                        "rx-rpc-generated-client-module.ts",
                        "tsconfig.json")
                .test();
    }

    @Test
    public void testSpecializedClientGeneration() {
        TestBundles.sampleSpecializedEndpointTester()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-generic-data.ts",
                        "sample-generic-list.ts",
                        "sample-generic-endpoint.ts",
//                        "sample-specialized-data.ts",
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
    @UseLogLevel(LogLevel.TRACE)
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
    public void testEndpointModuleGeneration() {
        TestBundles.sampleEndpointModuleTester()
                .apply(this::typeScriptOptions)
                .expectedFiles("rx-rpc-generated-client-module.ts")
                .test();
    }

    @Test
    public void testCircularReferenceEndpointGeneration() {
        TestBundles.sampleCircularReferenceDataEndpoint()
                .apply(this::typeScriptOptions)
                .expectedFiles(
                        "sample-circular-reference-data.ts",
                        "sample-circular-reference-endpoint.ts")
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
