import {InjectionToken, Injector, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {RxRpcClient, RxRpcClientModule, RxRpcInvoker, RxRpcTransport} from 'ng-rxrpc';
import { SampleEndpointClient } from './sample-endpoint-client';

export const RxRpcGeneratedClientModuleRXRPC_INVOKER = new InjectionToken<RxRpcInvoker>('RxRpcGeneratedClientModule.RxRpcInvoker');

@NgModule({
    imports: [ RxRpcClientModule ],
    providers: [
        SampleEndpointClient.provider(RxRpcGeneratedClientModuleRXRPC_INVOKER)
    ]
})
export class RxRpcGeneratedClientModule {

    public static withTransport(transport: Type<RxRpcTransport>|InjectionToken<RxRpcTransport>): ModuleWithProviders<RxRpcGeneratedClientModule> {
        return {
            ngModule: RxRpcGeneratedClientModule,
            providers: [{
                provide: RxRpcGeneratedClientModuleRXRPC_INVOKER,
                useFactory: RxRpcGeneratedClientModuleRxRpcInvokerFactory,
                deps: [transport]
            }]
        };
    }
}

export function RxRpcGeneratedClientModuleRxRpcInvokerFactory(t: RxRpcTransport) {
    return new RxRpcClient(t);
}
