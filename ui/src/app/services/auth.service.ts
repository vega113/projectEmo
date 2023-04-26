// src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from '../models/emotion.model';
import {tap} from "rxjs/operators";
import { BehaviorSubject, Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:4200/api';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated: Observable<boolean> = this.isAuthenticatedSubject.asObservable();


  constructor(private http: HttpClient) {
    const token = localStorage.getItem('auth_token');
    this.isAuthenticatedSubject.next(!!token);
  }

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
          this.isAuthenticatedSubject.next(true);
          console.log('User logged in successfully', response);
          localStorage.setItem('auth_token', response.token);
        })
      )
  }

  logout() {
    localStorage.removeItem('auth_token');
    this.isAuthenticatedSubject.next(false);
  }
}
