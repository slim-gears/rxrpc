import { SampleOptionalData, SampleOptionalDataEndpoint } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleOptionalDataEndpoint
 */
@Injectable()
export class SampleOptionalDataEndpointClient implements SampleOptionalDataEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public optionalDataMethod(): Observable<SampleOptionalData> {
        return this.invoker.invoke('sampleOptionalDataEndpoint/optionalDataMethod', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleOptionalDataEndpointClient,
            useFactory: SampleOptionalDataEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleOptionalDataEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleOptionalDataEndpointClient(invoker);
}
