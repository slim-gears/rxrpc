import {InjectionToken, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {RxRpcClient, RxRpcInvoker, RxRpcTransport} from 'rxrpc-js';
import { SampleEndpointClient } from './sample-endpoint-client';

@NgModule({
    providers: [
        SampleEndpointClient.provider(TestModule.RxRpcInvoker)
    ]
})
export class TestModule {
    public static readonly RxRpcInvoker = new InjectionToken<RxRpcInvoker>('TestModule.RxRpcInvoker');

    public static withTransport(transportToken: Type<RxRpcTransport>|InjectionToken<RxRpcTransport>): ModuleWithProviders<TestModule> {
        return {
            ngModule: TestModule,
            providers: [{
                provide: TestModule.RxRpcInvoker,
                useFactory: TestModule.invokerFactory,
                deps: [transportToken]
            }]
        };
    }

    public static invokerFactory(transport: RxRpcTransport) {
        return new RxRpcClient(transport);
    }
}
