import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { DisplayEmotionComponent } from './display-emotion.component';

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
    }).compileComponents();

    fixture = TestBed.createComponent(DisplayEmotionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display emotion details including notes and tags', () => {
    // set up test data
    const emotion = {
      emotion: { emotionId: 'Happy', emotionName: 'Happy',
        emotionDescription: 'Feeling good', emotionType: 'Positive' },
      intensity: 10,
      subEmotions: [{ subEmotionName: 'Excitement' }],
      triggers: [{ description: 'Birthday party' }],
      notes: [{ title: 'Great time', text: 'I had a great time' }],
      tags: [{ tagName: 'Birthday' }],
      created: new Date(),
    };
    component.emotion = emotion;

    // trigger change detection
    fixture.detectChanges();

    // test that the emotion details are displayed correctly
    const emotionType = fixture.nativeElement.querySelector('div:nth-child(2)').textContent;
    const emotionName = fixture.nativeElement.querySelector('div:nth-child(3)').textContent;
    const intensity = fixture.nativeElement.querySelector('div:nth-child(4)').textContent;
    const subEmotion = fixture.nativeElement.querySelector('div:nth-child(5)').textContent;
    const trigger = fixture.nativeElement.querySelector('div:nth-child(6)').textContent;
    const created = fixture.nativeElement.querySelector('div:nth-child(7)').textContent;
    const notesHeader = fixture.nativeElement.querySelector('div:nth-child(8) h3').textContent;
    const tagsHeader = fixture.nativeElement.querySelector('div:nth-child(9) h3').textContent;
    const note = fixture.nativeElement.querySelector('div:nth-child(8) ul li').textContent;
    const tag = fixture.nativeElement.querySelector('div:nth-child(9) ul li').textContent;

    expect(notesHeader).toContain('Notes:');
    expect(tagsHeader).toContain('Tags:');
    expect(note).toContain(emotion.notes[0].title);
    expect(note).toContain(emotion.notes[0].text);
    expect(tag).toContain(emotion.tags[0].tagName);

    expect(emotionType).toContain(emotion.emotion.emotionType);
    expect(emotionName).toContain(emotion.emotion.emotionName);
    expect(intensity).toContain(emotion.intensity);
    expect(subEmotion).toContain(emotion.subEmotions[0].subEmotionName);
    expect(trigger).toContain(emotion.triggers[0].description);
    expect(created).toContain(emotion.created.toDateString());
    expect(note).toContain(emotion.notes[0].text);
    expect(tag).toContain(emotion.tags[0].tagName);
  });
});
