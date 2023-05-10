import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import {EmotionData, EmotionRecord, Note, SuggestedAction} from '../models/emotion.model';
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
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    headers.set('Content-Type', 'application/json');
    return this.http
      .post<EmotionRecord>(`${this.apiUrl}/emotionRecord`, emotionRecord, { headers })
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  addNoteToEmotionRecord(emotionRecordId: number, note: Note) {
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    headers.set('Content-Type', 'application/json');
    return this.http
      .post<EmotionRecord>(`${this.apiUrl}/emotionRecord/${emotionRecordId}/note`, note, { headers })
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }
  fetchSuggestedActionsForEmotionRecord(emotionRecordId: number) {
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    return this.http
      .get<SuggestedAction[]>(`${this.apiUrl}/emotionRecord/${emotionRecordId}/suggestedActions`, { headers })
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

  fetchEmotionRecordsForCurrentUser() {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<EmotionRecord[]>(`${this.apiUrl}/emotionRecord/user`, { headers }).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.handleError(error);
      })
    );
  }
}
