import {InjectionToken, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {RxRpcClient, RxRpcInvoker, RxRpcTransport} from 'rxrpc-js';
import { SampleEndpointClient } from './sample-endpoint-client';

@NgModule({
    providers: [
        SampleEndpointClient.provider(RxRpcGeneratedClientModule.RxRpcInvoker)
    ]
})
export class RxRpcGeneratedClientModule {
    public static readonly RxRpcInvoker = new InjectionToken<RxRpcInvoker>('RxRpcGeneratedClientModule.RxRpcInvoker');

    public static withTransport(transportToken: Type<RxRpcTransport>|InjectionToken<RxRpcTransport>): ModuleWithProviders<RxRpcGeneratedClientModule> {
        return {
            ngModule: RxRpcGeneratedClientModule,
            providers: [{
                provide: RxRpcGeneratedClientModule.RxRpcInvoker,
                useFactory: RxRpcGeneratedClientModule.invokerFactory,
                deps: [transportToken]
            }]
        };
    }

    public static invokerFactory(transport: RxRpcTransport) {
        return new RxRpcClient(transport);
    }
}
