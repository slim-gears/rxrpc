import { SampleGenericData, SampleGenericList } from './index';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleGenericEndpoint
 */
export interface SampleGenericEndpoint<T> {
    genericMethod(data: T): Observable<T>;
    genericDataMethod(request: string): Observable<SampleGenericData<T>>;
    genericListMethod(): Observable<SampleGenericList<T>>;
    genericInputDataMethod(data: SampleGenericData<T>): Observable<void>;
}
