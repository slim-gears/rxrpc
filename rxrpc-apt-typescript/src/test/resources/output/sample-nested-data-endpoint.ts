import { SampleNestedDataEndpointData } from './sample-nested-data-endpoint-data';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleNestedDataEndpoint
 */
export interface SampleNestedDataEndpoint {
    observableDataMethod(): Observable<SampleNestedDataEndpointData>;
}
