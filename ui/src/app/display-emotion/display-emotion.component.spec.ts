import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayEmotionComponent } from './display-emotion.component';
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";


describe('DisplayEmotionComponent', () => {
  let component: DisplayEmotionComponent;
  let fixture: ComponentFixture<DisplayEmotionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientModule,
        ReactiveFormsModule,
        BrowserAnimationsModule
      ],
      declarations: [ DisplayEmotionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DisplayEmotionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display emotion details', () => {
    // set up test data
    const emotion = {
      emotion: { emotionId: 'Happy', emotionName: 'Happy', emotionDescription: 'Feeling good' },
      intensity: 10,
      subEmotions: [{ subEmotionName: 'Excitement' }],
      triggers: [{ description: 'Birthday party' }],
      created: new Date(),
    };
    component.emotion = emotion;

    // trigger change detection
    fixture.detectChanges();

    // test that the emotion details are displayed correctly
    const emotionName = fixture.nativeElement.querySelector('div:nth-child(2)').textContent;
    const intensity = fixture.nativeElement.querySelector('div:nth-child(3)').textContent;
    const subEmotion = fixture.nativeElement.querySelector('div:nth-child(4)').textContent;
    const trigger = fixture.nativeElement.querySelector('div:nth-child(5) ').textContent;
    const created = fixture.nativeElement.querySelector('div:nth-child(6)').textContent;

    expect(emotionName).toContain(emotion.emotion.emotionName);
    expect(intensity).toContain(emotion.intensity);
    expect(subEmotion).toContain(emotion.subEmotions[0].subEmotionName);
    expect(trigger).toContain(emotion.triggers[0].description);
    expect(created).toContain(emotion.created.toDateString());
  });


});
