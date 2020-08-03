import { SampleArray } from './sample-array';
import { SampleData } from './sample-data';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleArrayEndpoint
 */
export interface SampleArrayEndpoint {
    arrayObservableMethod(sampleData: SampleData): Observable<SampleArray[]>;
}
