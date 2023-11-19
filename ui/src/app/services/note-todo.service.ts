import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NoteTodoUpdate } from '../models/emotion.model';
import {map} from "rxjs/operators";
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class NoteTodoService {

  constructor(private http: HttpClient, private authService: AuthService,) { }

  updateNoteTodoToAccepted(noteTodoUpdate: NoteTodoUpdate): Observable<boolean> {
    const headers = this.authService.getAuthorizationHeader();
    return this.http.put(environment.baseUrl + `/note/todo/accept/${noteTodoUpdate.id}`,
      { }, {headers, observe: 'response' }).pipe(
      map(response => response.status === 200)
    )
  }
}
