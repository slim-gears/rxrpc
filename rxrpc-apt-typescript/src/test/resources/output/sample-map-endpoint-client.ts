import { SampleMapData, SampleMapEndpoint } from './index';
import { Injectable } from '@angular/core';
import { RxRpcClient } from 'ng-rxrpc';
import { Observable } from 'rxjs';
import { StringKeyMap } from 'rxrpc-types';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapEndpoint
 */
@Injectable()
export class SampleMapEndpointClient implements SampleMapEndpoint {
    constructor(private client: RxRpcClient) {
    }

    public mapDataMethod(arg: StringKeyMap<SampleMapData>): Observable<SampleMapData> {
        return this.client.invoke('sample-map-endpoint/mapDataMethod', {arg: arg});
    }

    public mapOfmapDataMethod(): Observable<StringKeyMap<SampleMapData>> {
        return this.client.invoke('sample-map-endpoint/mapOfmapDataMethod', {});
    }
}
