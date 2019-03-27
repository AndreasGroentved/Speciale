import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import {ProcurationComponent} from './procuration.component';

describe('ProcurationComponent', () => {
  let component: ProcurationComponent;
  let fixture: ComponentFixture<ProcurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProcurationComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
