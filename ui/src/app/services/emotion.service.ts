import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EmotionRecord } from '../models/emotion.model';

@Injectable({
  providedIn: 'root',
})
export class EmotionService {
  private apiUrl = 'http://localhost:9000/api';

  constructor(private http: HttpClient) {}

  getEmotionRecordsByUserId(userId: number) {
    return this.http.get<EmotionRecord[]>(`${this.apiUrl}/emotionRecord/user/${userId}`);
  }

  insertEmotionRecord(emotionRecord: Omit<EmotionRecord, 'id'>) {
    return this.http.post<EmotionRecord>(`${this.apiUrl}/emotionRecord`, emotionRecord);
  }
}
