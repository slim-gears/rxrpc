import { SampleNestedDataEndpoint } from './sample-nested-data-endpoint';
import { SampleNestedDataEndpointData } from './sample-nested-data-endpoint-data';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleNestedDataEndpoint
 */
@Injectable()
export class SampleNestedDataEndpointClient implements SampleNestedDataEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public observableDataMethod(): Observable<SampleNestedDataEndpointData> {
        return this.invoker.invoke('sampleNestedDataEndpoint/observableDataMethod', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleNestedDataEndpointClient,
            useFactory: SampleNestedDataEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleNestedDataEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleNestedDataEndpointClient(invoker);
}
