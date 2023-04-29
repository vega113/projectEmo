import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { EmotionRecord } from '../models/emotion.model';

@Injectable({
  providedIn: 'root',
})
export class EmotionStateService {
  private _newEmotionSubject = new BehaviorSubject<EmotionRecord | null>(null);
  newEmotionRecord$: Observable<EmotionRecord | null> = this._newEmotionSubject.asObservable();

  updateNewEmotion(emotion: EmotionRecord): void {
    this._newEmotionSubject.next(emotion);
  }
}
