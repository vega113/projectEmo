// src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from '../models/user.model';
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:4200/api';

  constructor(private http: HttpClient) {}

  signUp(user: User) {
    return this.http.post(`${this.apiUrl}/user`, user)
      .pipe(
        tap((response) => {
          console.log('User registered successfully', response);
          // Navigate to the login page or display a success message
        })
      );
  }

  login(username: string, password: string) {
    return this.http
      .post<{ token: string }>(`${this.apiUrl}/login`, { username, password })
      .pipe(
        tap((response) => {
          console.log('User logged in successfully', response);
          localStorage.setItem('auth_token', response.token);
        })
      )
  }
}
