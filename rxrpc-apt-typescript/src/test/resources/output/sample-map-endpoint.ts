import { SampleMapData } from './index';
import { StringKeyMap } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapEndpoint
 */
export interface SampleMapEndpoint {
    mapDataMethod(arg: StringKeyMap<SampleMapData>): Observable<SampleMapData>;
    mapOfmapDataMethod(): Observable<StringKeyMap<SampleMapData>>;
}
