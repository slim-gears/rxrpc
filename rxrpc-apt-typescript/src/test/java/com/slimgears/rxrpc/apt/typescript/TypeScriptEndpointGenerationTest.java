package com.slimgears.rxrpc.apt.typescript; /**
 *
 */

import com.slimgears.rxrpc.apt.AnnotationProcessingTester;
import com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor;
import org.junit.Test;
import org.slf4j.event.Level;

public class TypeScriptEndpointGenerationTest {
    @Test
    public void testEndpointClientServerGeneration() {
        AnnotationProcessingTester.create()
                .options(
                        "-Arxrpc.ts.ngmodule",
                        "-Arxrpc.ts.npm")
                .inputFiles(
                        "SampleBaseEndpoint.java",
                        "SampleArrayEndpoint.java",
                        "SampleArray.java",
                        "SampleEndpoint.java",
                        "SampleRequest.java",
                        "SampleData.java",
                        "SampleEnum.java")
                .expectedFiles(
                        "sample-endpoint-client.ts",
                        "sample-request.ts",
                        "sample-data.ts",
                        "sample-enum.ts",
                        "sample-array.ts",
                        "index.ts",
                        "module.ts",
                        "tsconfig.json")
                .processedWith(new RxRpcEndpointAnnotationProcessor())
                .verbosity(Level.TRACE)
                .test();
    }
}
