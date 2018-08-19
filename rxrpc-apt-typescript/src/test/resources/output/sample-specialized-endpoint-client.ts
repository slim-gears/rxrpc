import { SampleGenericData, SampleSpecializedEndpoint } from './index';
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

    public genericDataMethod(request: string): Observable<SampleGenericData<string>> {
        return this.client.invoke('sampleSpecializedEndpoint/genericDataMethod', {
            request: request
        });
    }

    public genericInputDataMethod(data: SampleGenericData<string>): Observable<void> {
        return this.client.invoke('sampleSpecializedEndpoint/genericInputDataMethod', {
            data: data
        });
    }
}
