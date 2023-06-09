import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpErrorResponse} from '@angular/common/http';
import { User } from '../models/emotion.model';
import { tap, catchError } from 'rxjs/operators';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import {JwtHelperService} from "@auth0/angular-jwt";
import {MatSnackBar} from "@angular/material/snack-bar";
import {environment} from "../../environments/environment";


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  public isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated: Observable<boolean> = this.isAuthenticatedSubject.asObservable();


  constructor(private http: HttpClient, private snackBar: MatSnackBar) {
    const token = localStorage.getItem('auth_token');
    this.isAuthenticatedSubject.next(!!token);
  }

  signUp(user: User) {
    return this.http.post(`${environment.baseUrl}/user`, user)
      .pipe(
        tap((response) => {
          console.log('User registered successfully', response);
          // Navigate to the login page or display a success message
        })
      );
  }

  login(username: string, password: string) {
    return this.http
      .post<{ token: string }>(`${environment.baseUrl}/login`, { username, password })
      .pipe(
        tap((response) => {
          this.isAuthenticatedSubject.next(true);
          console.log('User logged in successfully', response);
          localStorage.setItem('auth_token', response.token);
        }),
        catchError((error: HttpErrorResponse) =>
          {
            this.snackBar.open('Invalid username or password', 'Close', {
              duration: 5000,
            });
            this.isAuthenticatedSubject.next(false);
            return throwError(() => new Error('Invalid username or password'));
          }
        )
      );
  }

  logout() {
    localStorage.removeItem('auth_token');
    this.isAuthenticatedSubject.next(false);
  }

  getAuthorizationHeader(): HttpHeaders {
    const token = localStorage.getItem('auth_token');
    if (token) {
      return new HttpHeaders({
        Authorization: `Bearer ${token}`,
      });
    } else {
      return new HttpHeaders();
    }
  }

  fetchDecodedToken() {
    const helper = new JwtHelperService();
    const encodedToken = localStorage.getItem('auth_token');
    return new JwtHelperService().decodeToken(encodedToken!);
  }
}
