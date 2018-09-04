import { SampleArray, SampleData, SampleEndpoint, SampleRequest } from './index';
import { Injectable } from '@angular/core';
import { RxRpcClient } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleEndpoint
 */
@Injectable()
export class SampleEndpointClient implements SampleEndpoint {
    constructor(private client: RxRpcClient) {
    }

    public futureStringMethod(msg: string, request: SampleRequest): Observable<string> {
        return this.client.invoke('sample-endpoint/futureStringMethod', {msg: msg, request: request});
    }

    public observableDataMethod(request: SampleRequest): Observable<SampleData> {
        return this.client.invoke('sample-endpoint/observableDataMethod', {request: request});
    }

    public intMethod(request: SampleRequest): Observable<number> {
        return this.client.invoke('sample-endpoint/intMethod', {request: request});
    }

    public arrayObservableMethod(sampleData: SampleData): Observable<SampleArray[]> {
        return this.client.invoke('sample-endpoint/arrayObservableMethod', {sampleData: sampleData});
    }
}
