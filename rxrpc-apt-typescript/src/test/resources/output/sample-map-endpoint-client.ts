import { SampleMapData, SampleMapEndpoint } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker, StringKeyMap } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapEndpoint
 */
@Injectable()
export class SampleMapEndpointClient implements SampleMapEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public mapDataMethod(arg: StringKeyMap<SampleMapData>): Observable<SampleMapData> {
        return this.invoker.invoke('sample-map-endpoint/mapDataMethod', {arg: arg});
    }

    public mapOfMapDataMethod(): Observable<StringKeyMap<SampleMapData>> {
        return this.invoker.invoke('sample-map-endpoint/mapOfMapDataMethod', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleMapEndpointClient,
            useFactory: SampleMapEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleMapEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleMapEndpointClient(invoker);
}
