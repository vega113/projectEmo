import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import {EmotionData, EmotionRecord} from '../models/emotion.model';
import { catchError, tap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import {ErrorService} from "./error.service";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class EmotionService {
  private apiUrl = 'http://localhost:4200/api';

  constructor(private http: HttpClient, private authService: AuthService, private errorService: ErrorService) {}

  getEmotionRecordsByUserId(userId: number) {
    const headers = this.authService.getAuthorizationHeader();
    return this.http
      .get<EmotionRecord[]>(`${this.apiUrl}/emotionRecord/user/${userId}`, { headers })
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  insertEmotionRecord(emotionRecord: Omit<EmotionRecord, 'id'>) {
    const headers = this.authService.getAuthorizationHeader();
    return this.http
      .post<EmotionRecord>(`${this.apiUrl}/emotionRecord`, emotionRecord, { headers })
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  getEmotionCache(): Observable<EmotionData> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http
      .get<EmotionData>(`${this.apiUrl}/emotionCache`, { headers })
      .pipe(
        tap((response) => {
          console.log('getEmotionCache', response);
        }),
        catchError(resp => this.errorService.handleError(resp))
      );
  }
}
