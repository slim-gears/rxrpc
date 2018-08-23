/**
 *
 */
package com.slimgears.rxrpc.sample;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.slimgears.rxrpc.core.RxRpcGenerate;
import io.reactivex.Observable;

@RxRpcGenerate(
        className = "SampleMeta${T}Endpoint",
        endpointName = "sampleMeta${T}Endpoint",
        value = {
                @RxRpcGenerate.Endpoint(params = String.class),
                @RxRpcGenerate.Endpoint(params = Integer.class),
                @RxRpcGenerate.Endpoint(className = "SampleMetaRequestEndpoint", params = SampleRequest.class),
                @RxRpcGenerate.Endpoint(className = "SampleMetaNotificationEndpoint", params = SampleNotification.class),
        }
)
public interface SampleMetaEndpoint<T> {
    class SampleData<T> {
        @JsonProperty public final T value;

        public SampleData(T value) {
            this.value = value;
        }
    }

    Observable<SampleData<T>> echoData(SampleData<T> data);
}
