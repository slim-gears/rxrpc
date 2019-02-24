import { SampleNestedDataEndpoint, SampleNestedDataEndpointData } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { RxRpcInvoker } from 'ng-rxrpc';
import { Observable } from 'rxjs';

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
