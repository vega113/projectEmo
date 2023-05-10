import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EmotionService} from '../services/emotion.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EmotionCacheService} from '../services/emotion-cache.service';
import {
  Emotion,
  EmotionData,
  EmotionRecord,
  EmotionTypesWithEmotions,
  EmotionWithSubEmotions, Note,
  SubEmotion,
  SubEmotionWithActions, Tag,
  Trigger
} from "../models/emotion.model";
import {AuthService} from "../services/auth.service";
import {from} from "rxjs";
import {EmotionStateService} from "../services/emotion-state.service";
import {Router} from "@angular/router";


@Component({
  selector: 'app-create-emotion',
  templateUrl: './create-emotion.component.html',
  styleUrls: ['./create-emotion.component.css']
})
export class CreateEmotionComponent implements OnInit {
  isLoadingEmotionCache: boolean = true;

  emotionForm: FormGroup;
  emotionIntensityValue: number = 1;
  sliderColor = 'rgba(75, 192, 192, 0.2)';

  emotionCache: EmotionData | undefined;

  emotionTypesWithEmotions: EmotionTypesWithEmotions[] | undefined;
  emotionWithSubEmotions: EmotionWithSubEmotions[] | undefined;

  constructor(private fb: FormBuilder, private emotionService: EmotionService, private authService: AuthService,
              private emotionStateService: EmotionStateService, private router: Router, private snackBar: MatSnackBar,
              private emotionCacheService: EmotionCacheService) {
    this.emotionForm = this.fb.group({
      emotionType: ['', Validators.required],
      intensity: [''],
      emotion: [''],
      trigger: [''],
      subEmotion: ['']
    });
  }

  ngOnInit(): void {
    this.emotionCacheService.emotionCache$.subscribe((cachedEmotionData) => {
      if (cachedEmotionData) {
        this.emotionCache = cachedEmotionData;
        this.isLoadingEmotionCache = false;
      } else {
        this.emotionService.getEmotionCache().subscribe({
          next: (emotionCache) => {
            this.emotionCache = emotionCache;
            this.emotionCacheService.updateEmotionCache(emotionCache);
          },
          error: (error) => {
            console.error('Error fetching emotion cache:', error);
            this.isLoadingEmotionCache = false;
            this.snackBar.open('Failed to fetch emotion cache', 'Close', {});
          },
          complete: () => {
            console.log('Emotion cache fetch completed');
            this.isLoadingEmotionCache = false;
          }
        });
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
          {
            next: (response) => {
              console.log('Emotion record inserted successfully', response);
              this.emotionStateService.updateNewEmotion(response);
              this.router.navigate(['/display-emotion']);
            },
            error: (error) => {
              console.error('Error inserting emotion record', error);
              this.snackBar.open('Failed to submit the emotion record', 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar']
              });
            }
          }
        )
      } catch (error) {
        console.error(error);
      }
    }
  }

  convertEmotionFromDataToEmotionRecord(emotionFromData: any): EmotionRecord {
    const decodedToken = this.authService.fetchDecodedToken();
    let emotion: any = null;
    if (emotionFromData.emotion?.emotion?.id) {
      emotion = {};
      emotion.id = emotionFromData.emotion.emotion.id;
    }
    const subEmotions: any[] = [];
    if (emotionFromData.subEmotion?.subEmotionId) {
      subEmotions.push({subEmotionId: emotionFromData.subEmotion.subEmotionId});
    }
    const triggers: any[] = [];
    if (emotionFromData.trigger?.triggerId) {
      triggers.push({triggerId: emotionFromData.trigger.triggerId});
    }
    const notes: any[] = [];
    if (emotionFromData.notes) {
      notes.push({note: emotionFromData.note.text});
    }
    const tags: any[] = [];

    return {
      userId: decodedToken.userId,
      emotionType: emotionFromData.emotionType,
      intensity: this.emotionIntensityValue,
      emotion: emotion as Emotion,
      subEmotions: subEmotions as SubEmotion[],
      triggers: triggers as Trigger[],
      notes: notes as Note[],
      tags: tags as Tag[]
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
    if (this.emotionCache && this.emotionCache.emotionTypes) {
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
