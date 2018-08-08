import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SendFieldComponent } from './send-field.component';

describe('SendFieldComponent', () => {
  let component: SendFieldComponent;
  let fixture: ComponentFixture<SendFieldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SendFieldComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SendFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
