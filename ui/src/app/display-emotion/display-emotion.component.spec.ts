import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { DisplayEmotionComponent } from './display-emotion.component';
import {Note} from "../models/emotion.model";
import {of} from "rxjs";
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";


class MockEmotionService {
  addNoteToEmotionsRecord(emotionId: string, note: Note) {
    return of({}); // Redurn an observable of an empty object
  }
}
class MockEmotionStateService {
  newEmotionRecord$ = of({
    emotionType: 'Positive',
    emotion: { emotionId: 'Happy', emotionName: 'Happy', emotionDescription: 'Feeling good' },
    intensity: 10,
    subEmotions: [{ subEmotionName: 'Excitement' }],
    triggers: [{ description: 'Birthday party' }],
    created: new Date(),
    notes: [],
    tags: [],
  });
  updateNewEmotion(updatedEmotion: any) {}
}



describe('DisplayEmotionComponent', () => {
  let component: DisplayEmotionComponent;
  let fixture: ComponentFixture<DisplayEmotionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
      ],
      declarations: [DisplayEmotionComponent],
      providers: [
        { provide: EmotionService, useClass: MockEmotionService },
        { provide: EmotionStateService, useClass: MockEmotionStateService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DisplayEmotionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show loading screen when isLoading is true', () => {
    component.isLoading = true;
    fixture.detectChanges();

    const loadingScreen = fixture.nativeElement.querySelector('.loading-screen');
    expect(loadingScreen).toBeTruthy();
  });

  it('should hide loading screen and show emotion details when isLoading is false', () => {
    component.isLoading = false;
    component.emotion = {
      emotionType: 'Positive',
      emotion: { id: 'Happy', emotionName: 'Happy' },
      intensity: 10,
      subEmotions: [{ subEmotionName: 'Excitement' }],
      triggers: [{ description: 'Birthday party' }],
      notes: [],
      tags: [],
      created: new Date().toDateString(),
    };
    fixture.detectChanges();

    const loadingScreen = fixture.nativeElement.querySelector('.loading-screen');
    expect(loadingScreen).toBeNull();

    const emotionType = fixture.nativeElement.querySelector('div:nth-child(2)').textContent;
    expect(emotionType).toContain(component.emotion.emotionType);
  });

// You can add more test cases to test the component's behavior.


});
