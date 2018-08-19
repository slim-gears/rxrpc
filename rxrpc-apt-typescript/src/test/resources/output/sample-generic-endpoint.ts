import { Observable } from 'rxjs';

export interface SampleGenericEndpoint<T> {
    genericMethod(data: T): Observable<T>;
}
