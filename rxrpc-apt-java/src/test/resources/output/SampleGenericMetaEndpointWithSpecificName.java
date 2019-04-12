package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import java.lang.String;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcGenerateAnnotationProcessor")
@RxRpcEndpoint(value = "sampleGenericMetaEndpointWithSpecificName", options = "rxrpc.java.server=true")
public interface SampleGenericMetaEndpointWithSpecificName extends SampleGenericMetaEndpoint<String> {
}
