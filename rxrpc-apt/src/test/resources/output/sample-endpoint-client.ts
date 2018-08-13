import { SampleData, SampleRequest } from './';
import { RxRpcClient } from 'ng-rxrpc';
import { Observable } from 'rxjs';

export class SampleEndpointClient {
    constructor(private client: RxRpcClient) {
    }

    public futureStringMethod(msg: string, request: SampleRequest): Observable<string> {
        return this.client.invoke('sampleEndpoint/futureStringMethod', {
            msg: msg,
            request: request
        });
    }

    public intMethod(request: SampleRequest): Observable<number> {
        return this.client.invoke('sampleEndpoint/intMethod', {
            request: request
        });
    }

    public observableDataMethod(request: SampleRequest): Observable<SampleData> {
        return this.client.invoke('sampleEndpoint/observableDataMethod', {
            request: request
        });
    }
}
