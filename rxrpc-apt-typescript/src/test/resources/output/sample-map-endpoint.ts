import { SampleMapData } from './index';
import { Observable } from 'rxjs';
import { StringKeyMap } from 'rxrpc-types';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapEndpoint
 */
export interface SampleMapEndpoint {
    mapDataMethod(arg: StringKeyMap<SampleMapData>): Observable<SampleMapData>;
    mapOfmapDataMethod(): Observable<StringKeyMap<SampleMapData>>;
}
