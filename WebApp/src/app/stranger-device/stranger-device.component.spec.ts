import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StrangerDeviceComponent } from './stranger-device.component';

describe('StrangerDeviceComponent', () => {
  let component: StrangerDeviceComponent;
  let fixture: ComponentFixture<StrangerDeviceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StrangerDeviceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StrangerDeviceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
