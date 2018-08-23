import { SampleRequest } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleBaseEndpoint
 */
export interface SampleBaseEndpoint {
    intMethod(request: SampleRequest): Observable<number>;
}
