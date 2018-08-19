import { SampleArrayEndpoint, SampleBaseEndpoint, SampleData, SampleRequest } from './index';
import { Observable } from 'rxjs';

export interface SampleEndpoint extends SampleBaseEndpoint, SampleArrayEndpoint {
    futureStringMethod(msg: string, request: SampleRequest): Observable<string>;
    observableDataMethod(request: SampleRequest): Observable<SampleData>;
}
