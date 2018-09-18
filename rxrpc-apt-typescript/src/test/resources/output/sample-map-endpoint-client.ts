import { SampleMapData, SampleMapEndpoint } from './index';
import { Injectable } from '@angular/core';
import { RxRpcInvoker, StringKeyMap } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapEndpoint
 */
@Injectable()
export class SampleMapEndpointClient implements SampleMapEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public mapDataMethod(arg: StringKeyMap<SampleMapData>): Observable<SampleMapData> {
        return this.invoker.invoke('sample-map-endpoint/mapDataMethod', {arg: arg});
    }

    public mapOfmapDataMethod(): Observable<StringKeyMap<SampleMapData>> {
        return this.invoker.invoke('sample-map-endpoint/mapOfmapDataMethod', {});
    }
}
