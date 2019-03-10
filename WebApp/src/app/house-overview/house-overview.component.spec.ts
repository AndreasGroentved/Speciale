import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HouseOverviewComponent } from './house-overview.component';

describe('HouseOverviewComponent', () => {
  let component: HouseOverviewComponent;
  let fixture: ComponentFixture<HouseOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HouseOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HouseOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
