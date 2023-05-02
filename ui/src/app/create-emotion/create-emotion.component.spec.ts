import { ComponentFixture, TestBed } from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CreateEmotionComponent } from './create-emotion.component';
import { EmotionService } from '../services/emotion.service';

import { of } from 'rxjs';
import {Emotion, EmotionRecord, SubEmotion} from "../models/emotion.model";
import {MatSliderModule} from "@angular/material/slider";
import {HttpClientModule} from "@angular/common/http";
import {MatSelectModule} from "@angular/material/select";
import {MatOptionModule, MatRippleModule} from "@angular/material/core";
import {MatCardModule} from "@angular/material/card";
import { RouterTestingModule } from '@angular/router/testing';
import { routes } from '../app-routing.module';
import {AuthService} from "../services/auth.service";

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

class MockAuthService {
  fetchDecodedToken() {
    return {
      userId: 1,
      username: 'testuser',
    }
  }
}

describe('CreateEmotionComponent', () => {
  let component: CreateEmotionComponent;
  let fixture: ComponentFixture<CreateEmotionComponent>;
  let emotionService: EmotionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FormsModule, MatRippleModule, MatCardModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        BrowserAnimationsModule,
        MatSliderModule,
        HttpClientModule,
        MatSelectModule,
        MatOptionModule,
        RouterTestingModule.withRoutes(routes),
      ],
      declarations: [CreateEmotionComponent],
      providers: [{ provide: EmotionService, useClass: MockEmotionService },
        { provide: AuthService, useClass: MockAuthService }]
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
    localStorage.setItem('auth_token', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTI3NzY5MzksImlhdCI6MTY4MTI0MDkzOSwidXNlcklkIjozLCJ1c2VybmFtZSI6IkZpZnR5b25lQWRtaW5Vc2VyMSIsImVtYWlsIjoiRmlmdHlvbmVBZG1pblVzZXIxQGVtYWlsLmNvbSIsImZpcnN0bmFtZSI6Ill1cmkiLCJsYXN0bmFtZSI6IlVzZXIiLCJyb2xlIjoidXNlciJ9.brtjzMVjEv_h_MiZkCjuexDovZFBkm-eYlQdSAXR1n4');
    spyOn(emotionService, 'insertEmotionRecord').and.callThrough();

    component.emotionForm.controls["emotionType"].setValue('positive');
    component.emotionForm.controls["intensity"].setValue(5);
    component.changeSliderColor({ target: { valueAsNumber: 5 } })

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

    expect(component.sliderColor).toBe('rgb(128, 128, 0)');
    expect(component.emotionIntensityValue).toBe(5);
  });

  it('should submit the form with all optional values', () => {
    spyOn(emotionService, 'insertEmotionRecord').and.callThrough();

    component.emotionForm.controls['emotionType'].setValue("positive");
    component.emotionForm.controls['intensity'].setValue(5);
    component.emotionForm.controls['emotion'].setValue({emotion: {"id": "Joy", "emotionType": "positive", "emotionName": "Joy"} as Emotion});
    component.emotionForm.controls['trigger'].setValue({"triggerId": 2, "triggerName": "Family"});
    component.emotionForm.controls['subEmotion'].setValue({"subEmotionId": "Gratitude", "subEmotionName": "Gratitude"} as SubEmotion);
    component.changeSliderColor({ target: { valueAsNumber: 5 } })

    component.onSubmit();

    const expectedData: any = {
      userId: 1,
      intensity: 5,
      emotion: {"id": "Joy", "emotionType": "positive"},
      triggers: [{ triggerId: 2}],
      subEmotions: [{ subEmotionId: 'Gratitude'}],
      notes: [],
      tags: [],
    };

    expect(emotionService.insertEmotionRecord).toHaveBeenCalledWith(expectedData);
  });
});
