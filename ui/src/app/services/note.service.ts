import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NoteTemplate } from '../models/emotion.model';
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment"; // adjust the path as needed

@Injectable({
  providedIn: 'root',
})
export class NoteService {
  private apiUrl = '/api/noteTemplate';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getNoteTemplates(): Observable<NoteTemplate[]> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<NoteTemplate[]>(environment.baseUrl, { headers });
  }
}
