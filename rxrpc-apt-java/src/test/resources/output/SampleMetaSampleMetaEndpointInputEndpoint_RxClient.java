package com.slimgears.rxrpc.sample;

import com.google.common.reflect.TypeToken;
import com.slimgears.rxrpc.client.AbstractClient;
import com.slimgears.rxrpc.client.RxClient.Session;
import io.reactivex.Observable;
import java.lang.String;
import java.util.List;
import javax.annotation.Generated;

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMetaSampleMetaEndpointInputEndpoint
 */
@Generated("com.slimgears.rxrpc.apt.RxRpcEndpointAnnotationProcessor")
public class SampleMetaSampleMetaEndpointInputEndpoint_RxClient extends AbstractClient implements SampleMetaSampleMetaEndpointInputEndpoint {
    public SampleMetaSampleMetaEndpointInputEndpoint_RxClient(Session session) {
        super(session);
    }

    @Override
    public Observable<List<SampleMetaEndpointInput>> data( String data) {
        return invokeObservable(InvocationInfo
            .builder(new TypeToken<List<SampleMetaEndpointInput>>(){})
            .method("sampleMetaSampleMetaEndpointInputEndpoint/data")
            .shared(false)
            .sharedReplayCount(0)
            .arg("data", data)
            .build());
    }

}
