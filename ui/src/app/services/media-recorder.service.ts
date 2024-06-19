import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {AuthService} from "./auth.service";
import {ErrorService} from "./error.service";
import {TranscribedText} from "../models/emotion.model";
import {environment} from "../../environments/environment";
import {catchError} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class MediaRecorderService {
  private mediaRecorder: MediaRecorder | undefined
  private audioChunks: Blob[] = [];

  constructor(private http: HttpClient, private authService: AuthService, private errorService: ErrorService) {
  }

  startRecording() {
    navigator.mediaDevices.getUserMedia({ audio: true }).then(stream => {
      this.mediaRecorder = new MediaRecorder(stream);
      this.mediaRecorder.start();

      this.mediaRecorder.addEventListener('dataavailable', event => {
        this.audioChunks.push(event.data);
      });
    });
  }

  stopRecording(): Promise<Blob> {
    return new Promise(resolve => {
      this.mediaRecorder?.addEventListener('stop', () => {
        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        this.audioChunks = [];
        resolve(audioBlob);
      });

      this.mediaRecorder?.stop();
    });
  }

  transcribeAudio(audioBlob: Blob): Observable<TranscribedText> {
    const headers: HttpHeaders = this.authService.getAuthorizationHeader();
    const formData = new FormData();
    formData.append('audio', audioBlob, 'audio.webm');
    return this.http
      .post<TranscribedText>(`${environment.baseUrl}/transcribe`, formData, {headers})
      .pipe(catchError(resp => {
        return this.errorService.handleError(resp);
      }));
  }
}
