import { NgModule } from '@angular/core'
import { RxRpcClientModule } from 'ng-rxrpc'
import {
    SampleEndpointClient
} from './index';

@NgModule({
    imports: [ RxRpcClientModule ],
    providers: [
        SampleEndpointClient
    ]
})
export class RxRpcGeneratedClientModule {
}
