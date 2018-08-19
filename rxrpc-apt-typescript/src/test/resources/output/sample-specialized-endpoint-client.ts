import { SampleSpecializedEndpoint } from './index'

@Injectable()
export class SampleSpecializedEndpointClient implements SampleSpecializedEndpoint {
    constructor(private client: RxRpcClient) {
    }

    public genericMethod(data: string): Observable<string> {
        return this.client.invoke('sampleSpecializedEndpoint/genericMethod', {
            data: data
        });
    }
}
