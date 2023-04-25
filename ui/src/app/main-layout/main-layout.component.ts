import { Component } from '@angular/core';
import {Observable} from "rxjs";
import {AuthService} from "../services/auth.service";

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css', './landing-page-background.css']
})
export class MainLayoutComponent {
  public isAuthenticated: Observable<boolean>;

  constructor(private authService: AuthService) {
    this.isAuthenticated = this.authService.isAuthenticated;
  }

  logout() {
    this.authService.logout();
  }
}
