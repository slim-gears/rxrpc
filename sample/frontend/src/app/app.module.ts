import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {BackendApiModule} from "../backend-api";
import {RxRpcHttpTransport, RxRpcReconnectableTransport, RxRpcWebSocketTransport} from "rxrpc-js";
import {APP_BASE_HREF} from "@angular/common";
import {environment} from "../environments/environment";
import * as log from "loglevel"

const transports = {
  http: RxRpcHttpTransport,
  https: RxRpcHttpTransport,
  ws: RxRpcReconnectableTransport
};

function getCookie(name: string): string {
  const nameLenPlus = (name.length + 1);
  return document.cookie
    .split(';')
    .map(c => c.trim())
    .filter(cookie => {
      return cookie.substring(0, nameLenPlus) === `${name}=`;
    })
    .map(cookie => {
      return decodeURIComponent(cookie.substring(nameLenPlus));
    })[0] || null;
}

function getTransportType() {
  return getCookie("RxRpcTransport") || environment.transport;
}

function getTransportToken() {
  const transport = getTransportType();
  log.info('Selected transport: ', transport)
  return transports[transport];
}

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BackendApiModule.withTransport(getTransportToken())
  ],
  providers: [
    {provide: RxRpcWebSocketTransport, useFactory: () => new RxRpcWebSocketTransport(`ws://${location.host}/api/ws`)},
    {provide: RxRpcReconnectableTransport, useFactory: (transport) => RxRpcReconnectableTransport.of(transport), deps: [RxRpcWebSocketTransport]},
    {provide: RxRpcHttpTransport, useFactory: () => new RxRpcHttpTransport(getTransportType() + `://${location.host}/api/http`, {observeEnabled: true, interceptors: []})},
    {provide: APP_BASE_HREF, useValue: '/'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
