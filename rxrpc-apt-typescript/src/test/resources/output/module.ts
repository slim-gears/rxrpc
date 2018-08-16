import { NgModule } from '@angular/core';
import { RxRpcClientModule } from 'ng-rxrpc';
import { SampleEndpointClient } from './sample-endpoint-client';

@NgModule({
    imports: [ RxRpcClientModule ],
    providers: [
        SampleEndpointClient
    ]
})
export class RxRpcGeneratedClientModule {
}
