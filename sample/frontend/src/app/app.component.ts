import {Component, OnInit} from '@angular/core';
import {SampleEndpointClient} from '../backend-api';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  message1: Observable<string>;
  message2: Observable<string>;
  message3: Observable<string>;
  message4: Observable<string>;

  constructor(private endpoint: SampleEndpointClient) {
  }

  ngOnInit(): void {
    this.message1 = this.endpoint.sayHello('Angular1');
    this.message2 = this.endpoint.sayHello('Angular2');
    this.message3 = this.endpoint.sayHello('Angular3');
    this.message4 = this.endpoint.sayHello('Angular4');
  }
}
