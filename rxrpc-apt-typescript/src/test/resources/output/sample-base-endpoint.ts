import { SampleRequest } from './sample-request';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleBaseEndpoint
 */
export interface SampleBaseEndpoint {
    intMethod(request: SampleRequest): Observable<number>;
}
