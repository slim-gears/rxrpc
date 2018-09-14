package com.slimgears.rxrpc.sample;

import com.slimgears.rxrpc.core.RxRpcEndpoint;
import java.lang.Integer;
import java.util.List;
import javax.annotation.Generated;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericMetaEndpointClass
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcGenerateAnnotationProcessor")
@RxRpcEndpoint(options = "rxrpc.java.server=true")
public class SampleGenericMetaEndpointClass_Of_Integer extends SampleGenericMetaEndpointClass<Integer> {

    @Inject
    public SampleGenericMetaEndpointClass_Of_Integer(@Named("test") List<Integer> items) {
        super(Integer.class, items);
    }
}
