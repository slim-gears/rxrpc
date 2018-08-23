import { SampleNestedDataEndpointData } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleNestedDataEndpoint
 */
export interface SampleNestedDataEndpoint {
    observableDataMethod(): Observable<SampleNestedDataEndpointData>;
}
