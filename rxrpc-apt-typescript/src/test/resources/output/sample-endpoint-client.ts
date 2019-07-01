import { SampleArray, SampleData, SampleEndpoint, SampleRequest } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';
import { RxRpcInvoker } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleEndpoint
 */
@Injectable()
export class SampleEndpointClient implements SampleEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public intMethod(request: SampleRequest): Observable<number> {
        return this.invoker.invoke('sample-endpoint/intMethod', {request: request});
    }

    public arrayObservableMethod(sampleData: SampleData): Observable<SampleArray[]> {
        return this.invoker.invoke('sample-endpoint/arrayObservableMethod', {sampleData: sampleData});
    }

    public futureStringMethod(msg: string, request: SampleRequest): Observable<string> {
        return this.invoker.invoke('sample-endpoint/futureStringMethod', {msg: msg, request: request});
    }

    public futureBooleanMethod(): Observable<boolean> {
        return this.invoker.invoke('sample-endpoint/futureBooleanMethod', {});
    }

    public observableDataMethod(request: SampleRequest): Observable<SampleData> {
        return this.invoker.invokeShared('sample-endpoint/observableDataMethod', 0, {request: request});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleEndpointClient,
            useFactory: SampleEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleEndpointClient(invoker);
}
