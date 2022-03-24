import {InjectionToken, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {RxRpcClient, RxRpcInvoker, RxRpcTransport, RxRpcClientOptions} from 'rxrpc-js';
import { SampleEndpointClient } from './sample-endpoint-client';

@NgModule({
    providers: [
        SampleEndpointClient.provider(RxRpcGeneratedClientModule.RxRpcInvoker)
    ]
})
export class RxRpcGeneratedClientModule {
    public static readonly RxRpcInvoker = new InjectionToken<RxRpcInvoker>('RxRpcGeneratedClientModule.RxRpcInvoker');

    public static withTransport(transportToken: Type<RxRpcTransport>|InjectionToken<RxRpcTransport>, options?: RxRpcClientOptions): ModuleWithProviders<RxRpcGeneratedClientModule> {
        return {
            ngModule: RxRpcGeneratedClientModule,
            providers: [{
                provide: RxRpcGeneratedClientModule.RxRpcInvoker,
                useFactory: transport => RxRpcGeneratedClientModule.invokerFactory(transport, options),
                deps: [transportToken]
            }]
        };
    }

    public static invokerFactory(transport: RxRpcTransport, options?: RxRpcClientOptions) {
        return new RxRpcClient(transport, options);
    }
}
