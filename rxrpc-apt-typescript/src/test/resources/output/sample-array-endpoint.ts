import { SampleArray, SampleData } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleArrayEndpoint
 */
export interface SampleArrayEndpoint {
    arrayObservableMethod(sampleData: SampleData): Observable<SampleArray[]>;
}
