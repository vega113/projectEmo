import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import {AuthService} from "./auth.service";
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  constructor(private authService: AuthService, private router: Router) {}

  handleError(error: HttpErrorResponse, message?: string) {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(error);
    if (error.status === 401) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
    return throwError(() => new Error(message || 'Something went wrong'));
  }
}
