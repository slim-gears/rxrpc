import { SampleDefaultNameEndpoint } from './index';
import { FactoryProvider, Injectable, InjectionToken, Type } from '@angular/core';
import { RxRpcInvoker } from 'ng-rxrpc';
import { Observable } from 'rxjs';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleDefaultNameEndpoint
 */
@Injectable()
export class SampleDefaultNameEndpointClient implements SampleDefaultNameEndpoint {
    constructor(private invoker: RxRpcInvoker) {
    }

    public method(): Observable<number> {
        return this.invoker.invoke('sample-default-name-endpoint/method', {});
    }

    public static provider(invokerToken: Type<RxRpcInvoker>|InjectionToken<RxRpcInvoker>): FactoryProvider {
        return {
            provide: SampleDefaultNameEndpointClient,
            useFactory: SampleDefaultNameEndpointClientFactory,
            deps: [invokerToken]
        };
    }
}

export function SampleDefaultNameEndpointClientFactory(invoker: RxRpcInvoker) {
    return new SampleDefaultNameEndpointClient(invoker);
}
