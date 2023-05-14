import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmotionCalendarComponent } from './emotion-calendar.component';

describe('EmotionCalendarComponent', () => {
  let component: EmotionCalendarComponent;
  let fixture: ComponentFixture<EmotionCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmotionCalendarComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmotionCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
