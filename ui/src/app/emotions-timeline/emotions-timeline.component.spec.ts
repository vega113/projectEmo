import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmotionsTimelineComponent } from './emotions-timeline.component';

describe('EmotionsTimelineComponent', () => {
  let component: EmotionsTimelineComponent;
  let fixture: ComponentFixture<EmotionsTimelineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmotionsTimelineComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmotionsTimelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
