import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoteTodoComponent } from './note-todo.component';

describe('NoteTodoComponent', () => {
  let component: NoteTodoComponent;
  let fixture: ComponentFixture<NoteTodoComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NoteTodoComponent]
    });
    fixture = TestBed.createComponent(NoteTodoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
