import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {EmotionDetectionResult, EmotionFromNoteResult, Note, NoteTemplate} from '../models/emotion.model';
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment";
import {catchError, map} from "rxjs/operators";
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

  detectEmotion(text: string): Observable<EmotionFromNoteResult> {
    const headers = this.authService.getAuthorizationHeader();
    const note: Note = {text: text};

    console.log('Detecting emotion for text url: ' + `${environment.baseUrl}/note/emotion/detect`);
    return this.http.post<EmotionDetectionResult>(
      `${environment.baseUrl}/note/emotion/detect`, note,
      {headers}).pipe(catchError(resp => this.errorService.handleError(resp))).
      pipe(map((emotionDetectionResult: EmotionDetectionResult) => {
        return {emotionDetection: emotionDetectionResult, note: note} as EmotionFromNoteResult;
      }));
  }
}
