import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EmotionService} from '../services/emotion.service';
import {
  EmotionData,
  EmotionRecord,
  EmotionTypesWithEmotions,
  EmotionWithSubEmotions, SubEmotionWithActions, Trigger
} from "../models/emotion.model";
import {AuthService} from "../services/auth.service";
import {filter, from} from "rxjs";
import {tap} from "rxjs/operators";
import {EmotionStateService} from "../services/emotion-state.service";
import {Router} from "@angular/router";


@Component({
  selector: 'app-create-emotion',
  templateUrl: './create-emotion.component.html',
  styleUrls: ['./create-emotion.component.css']
})
export class CreateEmotionComponent implements OnInit {
  emotionForm: FormGroup;
  emotionIntensityValue: number = 1;
  sliderColor = 'rgba(75, 192, 192, 0.2)';

  emotionCache: EmotionData | undefined;

  emotionTypesWithEmotions: EmotionTypesWithEmotions[] | undefined;
  emotionWithSubEmotions: EmotionWithSubEmotions[] | undefined;

  constructor(private fb: FormBuilder, private emotionService: EmotionService, private authService: AuthService,
      private emotionStateService: EmotionStateService, private router: Router) {
    this.emotionForm = this.fb.group({
      emotionType: ['', Validators.required],
      intensity: [''],
      emotion: [''],
      trigger: [''],
      subEmotion: ['']
    });
  }

  ngOnInit(): void {
    this.emotionService.getEmotionCache().subscribe({
      next: (emotionCache) => {
        this.emotionCache = emotionCache;
      },
      error: (error) => {
        console.error('Error fetching emotion cache:', error);
      },
      complete: () => {
        console.log('Emotion cache fetch completed');
      }
    });
  }

  async onSubmit(): Promise<void> {
    if (this.emotionForm.valid) {
      const emotionFromData = this.emotionForm.value;
      const emotionRecord = this.convertEmotionFromDataToEmotionRecord(emotionFromData);
      console.log(`Emotion record to be inserted: ${JSON.stringify(emotionRecord)}`);
      try {
        await from(this.emotionService.insertEmotionRecord(emotionRecord)).subscribe(
          (response) => {
            console.log('Emotion record inserted successfully', response);
            this.emotionStateService.updateNewEmotion(emotionRecord);
            this.router.navigate(['/display-emotion']);
          },
          (error) => {
            console.error('Error inserting emotion record', error);
          }
        )
      } catch (error) {
        console.error(error);
      }
    }
  }

  convertEmotionFromDataToEmotionRecord(emotionFromData: any): EmotionRecord {
    const decodedToken = this.authService.fetchDecodedToken();
    return {
      userId: decodedToken.userId,
      emotionId: emotionFromData.emotion.emotion.id,
      intensity: this.emotionIntensityValue,
      subEmotions: [{"subEmotionId": emotionFromData.subEmotion.subEmotionId}],
      triggers: [{"triggerId": emotionFromData.trigger.triggerId}],
    };
  }

  changeSliderColor(event: any) {
    const intensity = (event.target as HTMLInputElement).valueAsNumber;
    const r = Math.round(255 * (intensity / 10));
    const g = Math.round(255 * (1 - intensity / 10));
    this.sliderColor = `rgb(${r}, ${g}, 0)`;
    if (intensity) {
      this.emotionIntensityValue = intensity;
    }
  }

  makeEmotionTypesList(): string[] {
    if (this.emotionCache) {
      this.emotionTypesWithEmotions = this.emotionCache.emotionTypes;
      return this.emotionCache.emotionTypes.map(emotionTypeObject => emotionTypeObject.emotionType);
    } else {
      return [];
    }
  }

  makeEmotionsList(): EmotionWithSubEmotions[] {
    if (this.emotionCache && this.emotionTypesWithEmotions) {
      const selectedEmotionType = this.emotionForm.get('emotionType')?.value;
      const emotionTypeObject = this.emotionTypesWithEmotions.find(emotionTypeObject => emotionTypeObject.emotionType === selectedEmotionType);
      if (emotionTypeObject) {
        this.emotionWithSubEmotions = emotionTypeObject.emotions;
        return emotionTypeObject.emotions;
      } else {
        return [];
      }
    } else {
      return [];
    }
  }

  makeSubEmotionsList(): SubEmotionWithActions[] {
    if (this.emotionCache) {
      const selectedEmotionObject = this.emotionForm.get('emotion')?.value as EmotionWithSubEmotions;
      if (selectedEmotionObject) {
        return selectedEmotionObject.subEmotions;
      } else {
        return [];
      }
    } else {
      return [];
    }
  }

  makeTriggersList(): Trigger[] {
    if (this.emotionCache) {
      return this.emotionCache.triggers;
    } else {
      return [];
    }
  }
}
