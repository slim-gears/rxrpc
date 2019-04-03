import { SampleCircularReferenceData } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleCircularReferenceEndpoint
 */
export interface SampleCircularReferenceEndpoint {
    observableDataMethod(): Observable<SampleCircularReferenceData>;
}
