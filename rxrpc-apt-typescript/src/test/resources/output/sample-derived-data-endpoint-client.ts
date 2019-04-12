import { SampleDerivedData, SampleDerivedDataEndpoint } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleDerivedDataEndpoint
 */
@Injectable()
export class SampleDerivedDataEndpointClient implements SampleDerivedDataEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public observableDataMethod(): Observable<SampleDerivedData> {
        return this.invoker.invoke('sampleEndpoint/observableDataMethod', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleDerivedDataEndpointClient,
            useFactory: SampleDerivedDataEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleDerivedDataEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleDerivedDataEndpointClient(invoker);
}
