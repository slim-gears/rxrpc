import { SampleDefaultNameEndpoint } from './index';
import { Injectable } from '@angular/core';
import { RxRpcClient } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleDefaultNameEndpoint
 */
@Injectable()
export class SampleDefaultNameEndpointClient implements SampleDefaultNameEndpoint {
    constructor(private client: RxRpcClient) {
    }

    public method(): Observable<number> {
        return this.client.invoke('sample-default-name-endpoint/method', {});
    }
}
