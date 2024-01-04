import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpErrorResponse} from '@angular/common/http';
import { User } from '../models/emotion.model';
import { tap, catchError } from 'rxjs/operators';
import {BehaviorSubject, Observable, of, throwError} from 'rxjs';
import {JwtHelperService} from "@auth0/angular-jwt";
import {MatSnackBar} from "@angular/material/snack-bar";
import {environment} from "../../environments/environment";


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  public isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated: Observable<boolean> = this.isAuthenticatedSubject.asObservable();
  jwtHelper = new JwtHelperService();
  currentUsername: string = '';

  constructor(private http: HttpClient, private snackBar: MatSnackBar) {
    const token = localStorage.getItem('auth_token');
    this.isAuthenticatedSubject.next(!!token);
  }

  signUp(user: User) {
    return this.http.post(`${environment.baseUrl}/user`, user)
      .pipe(
        tap((response) => {
          console.log('User registered successfully: ' + user, response);
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
          console.log('User logged in successfully', this.jwtHelper.decodeToken(response.token));
          this.currentUsername = username;
          localStorage.removeItem('auth_token');
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

  logout(): Observable<any> {
    const username = this.fetchDecodedToken().username;
    if (username != "") {
      const headers = this.getAuthorizationHeader();
      return this.http
        .post<{ token: string }>(`${environment.baseUrl}/logout`, { username }, {headers})
        .pipe(
          tap((response) => {
            console.log('User logged out successfully on server', this.jwtHelper.decodeToken(response.token));
            this.currentUsername = username;
            localStorage.removeItem('auth_token');
            this.isAuthenticatedSubject.next(false);
          }),
          catchError((error: HttpErrorResponse) => {
            console.log('User failed to logout on server', error);
            return throwError(() => new Error('User failed to logout on server'));
          })
        );
    } else {
      // If username is empty, return an Observable that immediately completes
      return of(null);
    }
  }


  getAuthorizationHeader(): HttpHeaders {
    const token = localStorage.getItem('auth_token');
    if (token) {
      return new HttpHeaders({
        Authorization: `Bearer ${token}`,
        IdempotencyKey: `${Math.random()}`,
      });
    } else {
      return new HttpHeaders();
    }
  }

  fetchDecodedToken() {
    const encodedToken = localStorage.getItem('auth_token');
    return this.jwtHelper.decodeToken(encodedToken!);
  }
}
