import { SampleDerivedData } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleDerivedDataEndpoint
 */
export interface SampleDerivedDataEndpoint {
    observableDataMethod(): Observable<SampleDerivedData>;
}
