import { SampleRequest } from './index';
import { Observable } from 'rxjs';

export interface SampleBaseEndpoint {
    intMethod(request: SampleRequest): Observable<number>;
}
