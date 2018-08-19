import { SampleSpecializedEndpoint } from './index';
import { Injectable } from '@angular/core';
import { RxRpcClient } from 'ng-rxrpc';
import { Observable } from 'rxjs';

@Injectable()
export class SampleSpecializedEndpointClient implements SampleSpecializedEndpoint {
    constructor(private client: RxRpcClient) {
    }

    public genericMethod(data: string): Observable<string> {
        return this.client.invoke('sampleSpecializedEndpoint/genericMethod', {
            data: data
        });
    }
}
