import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {BackendApiModule} from "../backend-api";
import {RxRpcWebSocketTransport} from "rxrpc-js";
import {APP_BASE_HREF} from "@angular/common";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BackendApiModule.withTransport(RxRpcWebSocketTransport)
  ],
  providers: [
    {provide: RxRpcWebSocketTransport, useFactory: () => new RxRpcWebSocketTransport(`ws://${location.host}/api/`)},
    {provide: APP_BASE_HREF, useValue: '/'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
