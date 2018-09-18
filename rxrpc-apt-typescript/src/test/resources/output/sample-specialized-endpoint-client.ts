import { SampleGenericData, SampleSpecializedEndpoint } from './index';
import { Injectable } from '@angular/core';
import { RxRpcInvoker } from 'ng-rxrpc';
import { Observable } from 'rxjs';

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

    public genericInputDataMethod(data: SampleGenericData<string>): Observable<void> {
        return this.invoker.invoke('sampleSpecializedEndpoint/genericInputDataMethod', {data: data});
    }
}
