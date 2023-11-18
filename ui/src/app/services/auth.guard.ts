import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { map, take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard  {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.authService.isAuthenticated.pipe(
      take(1),
      map(isAuthenticated => {
        // Check if the URL is the landing page
        if (state.url === '/' || state.url === '') {
          this.router.navigate(['/charts']);
          if (!isAuthenticated) {
            return true;  // Allow access to the landing page
          }
        }

        if (isAuthenticated) {
          return true;
        } else {
          // Redirect to the login page if the user is not authenticated
          this.router.navigate(['/login']);
          return false;
        }
      })
    );
  }
}
