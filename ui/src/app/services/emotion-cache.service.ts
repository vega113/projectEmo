import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { EmotionData } from '../models/emotion.model';

@Injectable({
  providedIn: 'root'
})
export class EmotionCacheService {
  private emotionCacheSource = new BehaviorSubject<EmotionData | undefined>(undefined);
  emotionCache$ = this.emotionCacheSource.asObservable();

  constructor() {}

  updateEmotionCache(emotionCache: EmotionData): void {
    this.emotionCacheSource.next(emotionCache);
  }
}
