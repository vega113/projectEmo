import { Component } from '@angular/core';
import { User } from '../models/emotion.model';
import { AuthService } from '../services/auth.service';
import { switchMap, first } from 'rxjs/operators';
import {firstValueFrom} from "rxjs";
import { Router } from '@angular/router';
import {MatSnackBar} from "@angular/material/snack-bar"; // Import Router here


@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  username = '';
  email = '';
  password = '';
  firstName = '';
  lastName = '';

  constructor(private authService: AuthService, private router: Router,
              private snackBar: MatSnackBar,
  ) { }

  signup() {
    const user: User = {
      username: this.username,
      email: this.email,
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
    };

    firstValueFrom(this.authService
      .signUp(user)
      .pipe(
        switchMap(() => this.authService.login(this.username, this.password)),
        first()
      ))
      .then(() => {
        this.router.navigate(['/']);
      })
      .catch((error) => {
        console.error('Error during signup:', error);
        // Handle error here, e.g. show an error message
        this.snackBar.open('Signup failed, try another username.', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      });
  }

}
