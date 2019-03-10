import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IotDeviceModuleComponent } from './iot-device-module.component';

describe('IotDeviceModuleComponent', () => {
  let component: IotDeviceModuleComponent;
  let fixture: ComponentFixture<IotDeviceModuleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IotDeviceModuleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IotDeviceModuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
