import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EmotionService} from '../services/emotion.service';
import {EmotionData, EmotionRecord} from "../models/emotion.model";
import {AuthService} from "../services/auth.service";
import {filter} from "rxjs";


@Component({
  selector: 'app-create-emotion',
  templateUrl: './create-emotion.component.html',
  styleUrls: ['./create-emotion.component.css']
})
export class CreateEmotionComponent implements OnInit {
  emotionForm: FormGroup;
  emotionIntensityValue: number = 0;
  sliderColor = 'rgba(75, 192, 192, 0.2)';

  emotionCache: EmotionData | undefined;

  constructor(private fb: FormBuilder, private emotionService: EmotionService, private authService: AuthService) {
    this.emotionForm = this.fb.group({
      emotionType: ['', Validators.required],
      intensity: [''],
      emotionId: [''],
      triggerId: [''],
      subEmotionId: ['']
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

  onSubmit(): void {
    if (this.emotionForm.valid) {
      const emotionFromData = this.emotionForm.value;
      const emotionRecord = this.convertEmotionFromDataToEmotionRecord(emotionFromData);
      this.emotionService.insertEmotionRecord(emotionRecord).subscribe(response => {
        // Handle success
        console.log(response);
      });
    }
  }

  convertEmotionFromDataToEmotionRecord(emotionFromData: any): EmotionRecord {
    const decodedToken = this.authService.fetchDecodedToken();
    return {
      userId: decodedToken.userId,
      emotionId: emotionFromData.emotionId,
      intensity: emotionFromData.intensity,
      subEmotions: [emotionFromData.subEmotionId],
      triggers: [emotionFromData.triggerId],
    };
  }

  changeSliderColor(event: any) {
    const intensity = (event.target as HTMLInputElement).valueAsNumber;
    const r = Math.round(255 * (intensity / 10));
    const g = Math.round(255 * (1 - intensity / 10));
    this.sliderColor = `rgb(${r}, ${g}, 0)`;
    this.emotionIntensityValue = intensity;
  }

  // protected readonly filter = filter;
}
