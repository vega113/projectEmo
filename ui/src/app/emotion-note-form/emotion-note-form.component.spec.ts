import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmotionNoteFormComponent } from './emotion-note-form.component';

describe('EmotionNoteFormComponent', () => {
  let component: EmotionNoteFormComponent;
  let fixture: ComponentFixture<EmotionNoteFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [EmotionNoteFormComponent]
    });
    fixture = TestBed.createComponent(EmotionNoteFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
