import { SampleGenericEndpoint, SampleSpecializedData } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleSpecializedEndpoint
 */
export interface SampleSpecializedEndpoint extends SampleGenericEndpoint<string> {
    data(): Observable<SampleSpecializedData>;
}
