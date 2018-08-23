/**
 *
 */
package com.slimgears.rxrpc.apt.java;

import com.slimgears.rxrpc.apt.AnnotationProcessingTester;
import com.slimgears.rxrpc.apt.DataClassGenerator;
import com.slimgears.rxrpc.apt.TestBundles;
import com.slimgears.rxrpc.apt.util.ServiceProvider;
import com.slimgears.rxrpc.apt.util.ServiceProviders;
import com.slimgears.rxrpc.core.util.Scope;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaEndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        TestBundles.sampleEndpointTester()
                .apply(this::javaOptions)
                .options("-Arxrpc.java.autoservice")
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
    public void testMetaEndpointReferencedTypeParamsGeneration() {
        DataClassGenerator dataClassGenerator = Mockito.mock(DataClassGenerator.class);

        try (Scope.Closable ignored = Scope.scope(builder -> builder
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

    private AnnotationProcessingTester javaOptions(AnnotationProcessingTester tester) {
        return tester.options(
                        "-Arxrpc.java.client",
                        "-Arxrpc.java.server");
    }
}
