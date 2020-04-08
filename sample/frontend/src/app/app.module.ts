import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {BackendApiModule} from "../backend-api";
import {RxRpcWebSocketTransport, RxRpcReconnectableTransport} from "rxrpc-js";
import {APP_BASE_HREF} from "@angular/common";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BackendApiModule.withTransport(RxRpcReconnectableTransport)
  ],
  providers: [
    {provide: RxRpcWebSocketTransport, useFactory: () => new RxRpcWebSocketTransport(`ws://${location.host}/api/`)},
    {provide: RxRpcReconnectableTransport, useFactory: (transport) => RxRpcReconnectableTransport.of(transport), deps: [RxRpcWebSocketTransport]},
    {provide: APP_BASE_HREF, useValue: '/'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
