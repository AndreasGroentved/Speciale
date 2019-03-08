import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IotdeviceComponent } from './iotdevice.component';

describe('IotdeviceComponent', () => {
  let component: IotdeviceComponent;
  let fixture: ComponentFixture<IotdeviceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IotdeviceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IotdeviceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
