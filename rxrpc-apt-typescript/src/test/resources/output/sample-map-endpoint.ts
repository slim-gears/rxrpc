import { SampleMapData } from './sample-map-data';
import { Observable } from 'rxjs';
import { StringKeyMap } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapEndpoint
 */
export interface SampleMapEndpoint {
    mapDataMethod(arg: StringKeyMap<SampleMapData>): Observable<SampleMapData>;
    mapOfMapDataMethod(): Observable<StringKeyMap<SampleMapData>>;
}
