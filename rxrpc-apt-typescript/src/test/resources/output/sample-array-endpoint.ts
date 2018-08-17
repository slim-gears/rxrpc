import { SampleArray, SampleData } from './index';
import { Observable } from 'rxjs';

export interface SampleArrayEndpoint {
    arrayObservableMethod(sampleData: SampleData): Observable<SampleArray[]>;
}
