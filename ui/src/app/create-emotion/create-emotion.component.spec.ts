import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CreateEmotionComponent } from './create-emotion.component';
import { EmotionService } from '../services/emotion.service';

import { of } from 'rxjs';
import {EmotionRecord} from "../models/emotion.model";
import {MatSliderModule} from "@angular/material/slider";

// Mock EmotionService
class MockEmotionService {
  getEmotionCache() {
    // return mocked data
    return of([]);
  }

  insertEmotionRecord(emotionRecordData: any) {
    // return mocked response
    return of({ success: true });
  }
}

describe('CreateEmotionComponent', () => {
  let component: CreateEmotionComponent;
  let fixture: ComponentFixture<CreateEmotionComponent>;
  let emotionService: EmotionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        BrowserAnimationsModule,
        MatSliderModule
      ],
      declarations: [CreateEmotionComponent],
      providers: [{ provide: EmotionService, useClass: MockEmotionService }],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateEmotionComponent);
    component = fixture.componentInstance;
    emotionService = TestBed.inject(EmotionService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form initially', () => {
    expect(component.emotionForm.valid).toBeFalsy();
  });

  it('should have a valid form after filling required fields', () => {
    component.emotionForm.controls["emotionType"].setValue('positive');
    component.emotionForm.controls["intensity"].setValue(5);

    expect(component.emotionForm.valid).toBeTruthy();
  });

  it('should submit the form successfully', () => {
    spyOn(emotionService, 'insertEmotionRecord').and.callThrough();

    component.emotionForm.controls["emotionType"].setValue('positive');
    component.emotionForm.controls["intensity"].setValue(5);

    component.onSubmit();

    expect(emotionService.insertEmotionRecord).toHaveBeenCalled();
  });

  it('should change the slider color', () => {
    const event = {
      target: {
        valueAsNumber: 5,
      },
    };

    component.changeSliderColor(event);

    expect(component.sliderColor).toBe('rgb(127, 127, 0)');
    expect(component.emotionIntensityValue).toBe(5);
  });

  it('should submit the form with all optional values', () => {
    spyOn(emotionService, 'insertEmotionRecord').and.callThrough();

    component.emotionForm.controls["emotionType"].setValue("positive");
    component.emotionForm.controls["intensity"].setValue(5);
    component.emotionForm.controls["emotionId"].setValue("Joy");
    component.emotionForm.controls["triggerId"].setValue(2);
    component.emotionForm.controls["subEmotionId"].setValue("Gratitude");
    component.onSubmit();

    const expectedData: any = {
      emotionType: "positive",
      intensity: 5,
      emotionId: "Joy",
      triggerId: 2,
      subEmotionId: "Gratitude",
    };

    expect(emotionService.insertEmotionRecord).toHaveBeenCalledWith(expectedData);
  });
});
