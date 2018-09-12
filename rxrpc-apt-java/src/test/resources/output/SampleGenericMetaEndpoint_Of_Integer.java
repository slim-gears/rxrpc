package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import java.lang.Integer;
import javax.annotation.Generated;

@Generated("com.slimgears.rxrpc.apt.RxRpcGenerateAnnotationProcessor")
@RxRpcEndpoint(value = "sampleGenericMetaEndpoint_of_Integer", options = "rxrpc.java.server=true")
public interface SampleGenericMetaEndpoint_Of_Integer extends SampleGenericMetaEndpoint<Integer> {
}
