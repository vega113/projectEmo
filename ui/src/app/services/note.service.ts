import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NoteTemplate } from '../models/emotion.model';
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment";
import {map} from "rxjs/operators"; // adjust the path as needed

@Injectable({
  providedIn: 'root',
})
export class NoteService {

  constructor(private http: HttpClient, private authService: AuthService) {}

  getNoteTemplates(): Observable<NoteTemplate[]> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.get<NoteTemplate[]>(environment.baseUrl + '/noteTemplate', { headers });
  }

  // add method to delete and undelete a note
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
}
