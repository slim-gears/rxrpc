import {Component, OnInit} from '@angular/core';
import {SampleEndpointClient} from "../backend-api";
import {Observable} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  messageFromServer: Observable<string>;

  constructor(private endpoint: SampleEndpointClient) {
  }

  ngOnInit(): void {
    this.messageFromServer = this.endpoint.sayHello('Angular');
  }
}
