/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.slimgears.apt.util.AnnotationProcessingTester;
import com.slimgears.apt.util.StoreWrittenFilesRule;
import com.slimgears.rxrpc.apt.DataClassGenerator;
import com.slimgears.rxrpc.apt.TestBundles;
import com.slimgears.rxrpc.apt.util.ServiceProvider;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.util.generic.Scope;
import com.slimgears.util.generic.ScopedInstance;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaEndpointGenerationTest {
    @ClassRule
    public final static StoreWrittenFilesRule storeWrittenFilesRule = StoreWrittenFilesRule
            .forPath("build/test-results/files");

    @Test
    public void testEndpointClientServerGeneration() {
        TestBundles.sampleEndpointTester()
                .apply(this::javaOptions)
                .expectedSources(
                        "SampleEndpoint_RxClient.java",
                        "SampleEndpoint_RxModule.java")
                .test();
    }

    @Test
    public void testSpecializedEndpointGeneration() {
        TestBundles.sampleSpecializedEndpointTester()
                .apply(this::javaOptions)
                .expectedSources(
                        "SampleSpecializedEndpoint_RxClient.java",
                        "SampleSpecializedEndpoint_RxModule.java")
                .test();
    }

    @Test
    public void testGenericMetaEndpointGeneration() {
        TestBundles.sampleGenericMetaEndpointTester()
                .apply(this::javaOptions)
                .expectedSources(
                        "SampleGenericMetaEndpoint_Of_Integer.java",
                        "SampleGenericMetaEndpointWithSpecificName.java")
                .test();
    }

    @Test
    public void testGenericMetaEndpointClassGeneration() {
        TestBundles.sampleGenericMetaEndpointClassTester()
                .apply(this::javaOptions)
                .expectedSources(
                        "SampleGenericMetaEndpointClass_Of_Integer.java",
                        "SampleGenericMetaEndpointClass_Of_Integer_RxModule.java")
                .test();
    }

    @Test
    public void testGenericMetaDefaultNameEndpointGeneration() {
        TestBundles.sampleMetaDefaultNameEndpointTester()
                .apply(this::javaOptions)
                .expectedSources("SampleGenericMetaDefaultNameEndpoint_Of_String.java")
                .test();
    }

    @Test
    public void testMetaEndpointReferencedTypeParamsGeneration() {
        DataClassGenerator dataClassGenerator = Mockito.mock(DataClassGenerator.class);

        try (ScopedInstance.Closeable ignored = Scope.scope(builder -> builder
                .bind(DataClassGenerator.class).toInstance(dataClassGenerator)
                .bind(ServiceProvider.class).toInstance(ServiceProviders.ofMultiple(
                        ServiceProviders::loadServicesWithServiceLoader,
                        ServiceProviders::loadWithServiceResolver)))) {

            TestBundles.sampleMetaEndpointTester()
                    .apply(this::javaOptions)
                    .expectedSources(
                            "SampleMetaSampleMetaEndpointInputEndpoint.java")
                    .test();

            verify(dataClassGenerator, times(1)).generate(any());
            verify(dataClassGenerator).generate(
                    argThat(context -> context.sourceTypeElement().getSimpleName().toString().equals("SampleMetaEndpointInput")));
        }
    }

    @Test
    public void testDefaultEndpointName() {
        TestBundles.sampleDefaultNameEndpointTester()
                .apply(this::javaOptions)
                .expectedSources(
                        "SampleDefaultNameEndpoint_RxClient.java",
                        "SampleDefaultNameEndpoint_RxModule.java")
                .test();
    }

    @Test
    public void testEndpointModule() {
        TestBundles.sampleEndpointModuleTester()
                .apply(this::javaOptions)
                .expectedFiles("test-rxrpc-module.txt")
                .test();
    }

    private AnnotationProcessingTester javaOptions(AnnotationProcessingTester tester) {
        return tester
                .verbosity(Level.TRACE);
    }
}
