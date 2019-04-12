import {InjectionToken, Injector, ModuleWithProviders, NgModule, Type} from '@angular/core';
import {RxRpcClient, RxRpcInvoker, RxRpcTransport} from 'rxrpc-js';
import { SampleEndpointClient } from './sample-endpoint-client';

export const TestModuleRXRPC_INVOKER = new InjectionToken<RxRpcInvoker>('TestModule.RxRpcInvoker');

@NgModule({
    providers: [
        SampleEndpointClient.provider(TestModuleRXRPC_INVOKER)
    ]
})
export class TestModule {

    public static withTransport(transport: Type<RxRpcTransport>|InjectionToken<RxRpcTransport>): ModuleWithProviders<TestModule> {
        return {
            ngModule: TestModule,
            providers: [{
                provide: TestModuleRXRPC_INVOKER,
                useFactory: TestModuleRxRpcInvokerFactory,
                deps: [transport]
            }]
        };
    }
}

export function TestModuleRxRpcInvokerFactory(t: RxRpcTransport) {
    return new RxRpcClient(t);
}
: RxRpcTransport) {
    return new RxRpcClient(t);
}
