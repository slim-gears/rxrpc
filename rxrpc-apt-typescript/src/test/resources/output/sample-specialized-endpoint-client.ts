import { SampleGenericData, SampleGenericList, SampleSpecializedData, SampleSpecializedEndpoint } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleSpecializedEndpoint
 */
@Injectable()
export class SampleSpecializedEndpointClient implements SampleSpecializedEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public genericMethod(data: string): Observable<string> {
        return this.invoker.invoke('sampleSpecializedEndpoint/genericMethod', {data: data});
    }

    public genericDataMethod(request: string): Observable<SampleGenericData<string>> {
        return this.invoker.invoke('sampleSpecializedEndpoint/genericDataMethod', {request: request});
    }

    public genericListMethod(): Observable<SampleGenericList<string>> {
        return this.invoker.invoke('sampleSpecializedEndpoint/genericListMethod', {});
    }

    public genericInputDataMethod(data: SampleGenericData<string>): Observable<void> {
        return this.invoker.invoke('sampleSpecializedEndpoint/genericInputDataMethod', {data: data});
    }

    public data(): Observable<SampleSpecializedData> {
        return this.invoker.invoke('sampleSpecializedEndpoint/data', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleSpecializedEndpointClient,
            useFactory: SampleSpecializedEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleSpecializedEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleSpecializedEndpointClient(invoker);
}
