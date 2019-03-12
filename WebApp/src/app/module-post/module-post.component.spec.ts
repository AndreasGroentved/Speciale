import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModulePostComponent } from './module-post.component';

describe('ModulePostComponent', () => {
  let component: ModulePostComponent;
  let fixture: ComponentFixture<ModulePostComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModulePostComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModulePostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
