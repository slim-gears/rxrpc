import {ComponentFixture, TestBed} from '@angular/core/testing';
import {AppComponent} from './app.component';
import {AppModule} from "./app.module";
import {RxRpcInvoker} from "rxrpc-js";
import {concat, NEVER, of} from "rxjs";
import {BackendApiModule} from "../backend-api";

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        AppModule
      ],
      providers: [
        { provide: BackendApiModule.RxRpcInvoker, useValue: <RxRpcInvoker> {
            invoke: () => {
              return of();
            },
            invokeShared<T>() {
              return concat(of("Test response"), NEVER)
            }
          }
        }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AppComponent);
  });

  it('should create the app', () => {
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should display returned text', () => {
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('p').textContent)
      .toContain('Test response');
  });
});
