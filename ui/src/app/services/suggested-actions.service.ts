import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SuggestedAction } from '../models/emotion.model';

@Injectable({
  providedIn: 'root'
})
export class SuggestedActionsService {

  constructor(private http: HttpClient) { }

  getSuggestedActions(emotionId: string): Observable<SuggestedAction[]> {
    // Assuming you have an API endpoint that takes emotionId as a parameter and returns a list of suggested actions
    return this.http.get<SuggestedAction[]>(`/api/suggested-actions/${emotionId}`);
  }
}
