import { SampleGenericEndpoint } from './sample-generic-endpoint';
import { SampleSpecializedData } from './sample-specialized-data';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleSpecializedEndpoint
 */
export interface SampleSpecializedEndpoint extends SampleGenericEndpoint<string> {
    data(): Observable<SampleSpecializedData>;
}
