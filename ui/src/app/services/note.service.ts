import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {delay, EMPTY, expand, filter, Observable, of, throwIfEmpty, timer} from 'rxjs';
import {EmotionDetectionResult, EmotionFromNoteResult, Note, NoteTemplate} from '../models/emotion.model';
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment";
import {catchError, map, retry, switchMap, take, tap} from "rxjs/operators";
import {ErrorService} from "./error.service"; // adjust the path as needed

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
      map((response: HttpResponse<EmotionDetectionResult>) => {
        return {emotionDetection: response.body, note: note} as EmotionFromNoteResult;
      })
    );
  }

  private handleDetectEmotionWithRetry(note: Note, headers: HttpHeaders): Observable<HttpResponse<EmotionDetectionResult>> {
    return timer(0, 3000).pipe(
      take(20), // limit the number of retries to 20
      switchMap(() => this.http.post<EmotionDetectionResult>(
        `${environment.baseUrl}/note/emotion/detect`, note,
        {headers, observe: 'response'})),
      tap(response => {
        if (response.status === 202) {
          console.log('Emotion detection still in progress, retrying...');
        } else if (response.status === 200) {
          console.log('Emotion detection completed');
        }
      }),
      filter(response => response.status === 200),
      take(1), // stop retrying after the first successful response
      throwIfEmpty(() => new Error('Exhausted number of retries without success')),
      catchError(resp => this.errorService.handleError(resp))
    );
  }
}
