import { SampleCircularReferenceData, SampleCircularReferenceEndpoint } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleCircularReferenceEndpoint
 */
@Injectable()
export class SampleCircularReferenceEndpointClient implements SampleCircularReferenceEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public observableDataMethod(): Observable<SampleCircularReferenceData> {
        return this.invoker.invoke('sample-circular-reference-endpoint/observableDataMethod', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleCircularReferenceEndpointClient,
            useFactory: SampleCircularReferenceEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleCircularReferenceEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleCircularReferenceEndpointClient(invoker);
}
