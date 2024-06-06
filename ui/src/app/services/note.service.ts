import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {delay, Observable, retry, of, mergeMap, timer, retryWhen} from 'rxjs';
import {
  EmotionFromNoteResult,
  EmotionRecord,
  Note,
  NoteTemplate
} from '../models/emotion.model';
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment";
import {catchError, map } from "rxjs/operators";
import {ErrorService} from "./error.service";
import { throwError } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class NoteService {

  constructor(private http: HttpClient, private authService: AuthService, private errorService: ErrorService) {}

  getNoteTemplates(): Observable<NoteTemplate[]> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<NoteTemplate[]>(environment.baseUrl + '/noteTemplate', { headers });
  }

  deleteNote(id: number): Observable<boolean> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.put(environment.baseUrl + '/note/delete/' + id, {}, { headers, observe: 'response' })
      .pipe(
        map(response => response.status === 200)
      );
  }

  undeleteNote(id: number): Observable<boolean> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.put(environment.baseUrl + '/note/undelete/' + id, {}, { headers, observe: 'response' })
      .pipe(
        map(response => response.status === 200)
      );
  }

  detectEmotion(note: Note): Observable<EmotionFromNoteResult> {
    console.log('Detecting emotion for text url: ' + `${environment.baseUrl}/note/emotion/detect`);
    const headers = this.authService.getAuthorizationHeader();
    return this.handleDetectEmotionWithRetry(note, headers).pipe(
      map((response: HttpResponse<EmotionRecord>) => {
        return {emotionRecord: response.body, note: note} as EmotionFromNoteResult;
      })
    );
  }

  private handleDetectEmotionWithRetry(note: Note, headers: HttpHeaders): Observable<HttpResponse<EmotionRecord>> {
    const request$ = this.http.post<EmotionRecord>(
      `${environment.baseUrl}/note/emotion/detect`, note,
      { headers, observe: 'response' }
    );

    return request$.pipe(
      mergeMap(response => {
        if (response.status === 202) {
          // Retry after a delay of 3 seconds if the status is 202
          return timer(3000).pipe(
            mergeMap(() => throwError(() => new Error('Retrying due to status 202')))
          );
        }
        // If the status is not 202, return the response as is
        return of(response);
      }),
      retry({
        count: 4,
        delay: (error, retryCount) => {
          if (error.message === 'Retrying due to status 202') {
            // Delay before retrying
            console.log('Retrying. Retry count: ' + retryCount);
            return timer(500);
          }
          // If the error is different, do not retry
          return throwError(() => error);
        }
      }),
      catchError(resp => {
        console.error('Server error, stopping retry.');
        return this.errorService.handleError(resp);
      })
    );
  }
}
