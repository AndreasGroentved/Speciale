import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TangleDevicesComponent } from './tangle-devices.component';

describe('TangleDevicesComponent', () => {
  let component: TangleDevicesComponent;
  let fixture: ComponentFixture<TangleDevicesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TangleDevicesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TangleDevicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
