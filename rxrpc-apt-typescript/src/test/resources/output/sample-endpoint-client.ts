import { SampleArray, SampleData, SampleEndpoint, SampleRequest } from './index';
import { Injectable } from '@angular/core';
import { RxRpcInvoker } from 'ng-rxrpc';
import { Observable } from 'rxjs';

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

    public observableDataMethod(request: SampleRequest): Observable<SampleData> {
        return this.invoker.invoke('sample-endpoint/observableDataMethod', {request: request});
    }
}
