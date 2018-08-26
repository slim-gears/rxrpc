import { SampleNestedDataEndpoint, SampleNestedDataEndpointData } from './index';
import { Injectable } from '@angular/core';
import { RxRpcClient } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleNestedDataEndpoint
 */
@Injectable()
export class SampleNestedDataEndpointClient implements SampleNestedDataEndpoint {
    constructor(private client: RxRpcClient) {
    }

    public observableDataMethod(): Observable<SampleNestedDataEndpointData> {
        return this.client.invoke('sampleNestedDataEndpoint/observableDataMethod', {});
    }
}
