import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {Router} from "@angular/router";
import {AuthService} from "../services/auth.service";
import { lastValueFrom } from 'rxjs';
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private router: Router,
              private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  async onSubmit() {
    if (this.loginForm.valid) {
      const { username, password } = this.loginForm.value;
      try {
        const response = await lastValueFrom(this.authService.login(username, password));
        // Store the JWT token in localStorage or another secure place
        localStorage.setItem('auth_token', response.token);

        // Redirect the user to the main app or another desired route
        this.router.navigate(['/create-emotion']);
      } catch (error) {
        // Handle any errors from the API, such as incorrect credentials
        console.error('Login failed:', error);
        this.snackBar.open('Login failed', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    }
  }
}
