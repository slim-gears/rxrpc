import { SampleNestedDataEndpoint, SampleNestedDataEndpointData } from './index';
import { Injectable } from '@angular/core';
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
}
