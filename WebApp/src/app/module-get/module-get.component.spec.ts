import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModuleGetComponent } from './module-get.component';

describe('ModuleGetComponent', () => {
  let component: ModuleGetComponent;
  let fixture: ComponentFixture<ModuleGetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModuleGetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModuleGetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
