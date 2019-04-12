import { SampleOptionalData } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleOptionalDataEndpoint
 */
export interface SampleOptionalDataEndpoint {
    optionalDataMethod(): Observable<SampleOptionalData>;
}
