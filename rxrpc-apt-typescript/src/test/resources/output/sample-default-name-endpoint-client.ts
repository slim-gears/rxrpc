import { SampleDefaultNameEndpoint } from './index';
import { Injectable } from '@angular/core';
import { RxRpcInvoker } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleDefaultNameEndpoint
 */
@Injectable()
export class SampleDefaultNameEndpointClient implements SampleDefaultNameEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public method(): Observable<number> {
        return this.invoker.invoke('sample-default-name-endpoint/method', {});
    }
}
