package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import java.lang.Double;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcGenerateAnnotationProcessor")
@RxRpcEndpoint(value = "sampleGenericMetaEndpoint_of_Double", options = "rxrpc.java.server=true")
public interface SampleGenericMetaEndpoint_Of_Double extends SampleGenericMetaEndpoint<Double> {
}
