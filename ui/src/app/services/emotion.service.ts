import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {
  EmotionData,
  EmotionRecord,
  EmotionRecordDay,
  Note,
  SuggestedAction, SunburstData
} from '../models/emotion.model';
import {catchError, map, tap} from 'rxjs/operators';
import {AuthService} from './auth.service';
import {ErrorService} from "./error.service";
import {Observable} from "rxjs";
import {DateService} from "./date.service";
import { startOfMonth, endOfMonth } from 'date-fns';
import {environment} from "../../environments/environment";


@Injectable({
  providedIn: 'root',
})
export class EmotionService {
  private apiUrl = 'http://localhost:4200/api';

  constructor(private http: HttpClient, private authService: AuthService, private errorService: ErrorService,
              private dateService: DateService) {
  }

  getEmotionRecordsByUserId(userId: number) {
    const headers = this.authService.getAuthorizationHeader();
    return this.http
      .get<EmotionRecord[]>(`${environment.baseUrl}/emotionRecord/user/${userId}`, {headers})
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  insertEmotionRecord(emotionRecord: Omit<EmotionRecord, 'id'>) {
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    headers.set('Content-Type', 'application/json');
    return this.http
      .post<EmotionRecord>(`${environment.baseUrl}/emotionRecord`, emotionRecord, {headers})
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  addNoteToEmotionRecord(emotionRecordId: number, note: Note) {
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    headers.set('Content-Type', 'application/json');
    return this.http
      .post<EmotionRecord>(`${environment.baseUrl}/emotionRecord/${emotionRecordId}/note`, note, {headers})
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  fetchSuggestedActionsForEmotionRecord(emotionRecordId: number) {
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    return this.http
      .get<SuggestedAction[]>(`${environment.baseUrl}/emotionRecord/${emotionRecordId}/suggestedActions`, {headers})
      .pipe(catchError(resp => this.errorService.handleError(resp)));
  }


  getEmotionCache(): Observable<EmotionData> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http
      .get<EmotionData>(`${environment.baseUrl}/emotionCache`, {headers})
      .pipe(
        tap((response) => {
          console.log('getEmotionCache', response);
        }),
        catchError(resp => this.errorService.handleError(resp))
      );
  }

  fetchEmotionRecordsForCurrentUser() {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<EmotionRecord[]>(`${environment.baseUrl}/emotionRecord/user`, {headers}).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.handleError(error);
      })
    );
  }

  fetchEmotionRecordDaysForCurrentUser() {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<any[]>(`${environment.baseUrl}/emotionRecord/user/days`, {headers}).pipe(
      map(data => {
        return data.map((recordDay: any) => {
          return {
            date: new Date(recordDay.date), // Convert date string to Date object
            records: recordDay.records, // Assume records are already in the correct format
          } as EmotionRecordDay;
        });
      }),
      catchError((error: HttpErrorResponse) => {
        return this.errorService.handleError(error);
      })
    );
  }

  fetchMonthEmotionRecordsForCurrentUser(date: Date) {
    const headers = this.authService.getAuthorizationHeader();
    const startOfMonthDateString = this.dateService.formatDateToIsoMonthStartEndString(date, startOfMonth);
    const endOfMonthDateString = this.dateService.formatDateToIsoMonthStartEndString(date, endOfMonth);

    return this.http.get<EmotionRecord[]>(
      `${environment.baseUrl}/emotionRecord/user/month/${startOfMonthDateString}/${endOfMonthDateString}`,
      {headers}).
    pipe(catchError(resp => this.errorService.handleError(resp)));
  }

  fetchEmotionSunburnChartDataForDateRange(dateRange: {start: string, end: string}) {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<SunburstData[]>(
      `${environment.baseUrl}/charts/user/month/${dateRange.start}/${dateRange.end}`,
      {headers}).
    pipe(catchError(resp => this.errorService.handleError(resp)));
  }
}
