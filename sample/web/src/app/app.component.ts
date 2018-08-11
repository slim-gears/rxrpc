import { Component } from '@angular/core';
// import { SampleEndpointClient } from '../../../build/generated/source/apt/main/typescript/sample-endpoint';
// import { SampleNotification } from '../../../build/generated/source/apt/main/typescript/sample-notification';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'web';
//  notifications: SampleNotification[] = [];

  // constructor(private client: SampleEndpointClient) {
  // }

  onSend(count: number) {
    // this.client.observableMethod({id: count, name: 'Test'})
    //     .subscribe(this.notifications.push.bind(this.notifications));
  }
}
