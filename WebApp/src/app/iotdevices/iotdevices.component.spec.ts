import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IotdevicesComponent } from './iotdevices.component';

describe('IotdevicesComponent', () => {
  let component: IotdevicesComponent;
  let fixture: ComponentFixture<IotdevicesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IotdevicesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IotdevicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
