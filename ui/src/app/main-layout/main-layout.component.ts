import {Component, ViewChild} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {AuthService} from "../services/auth.service";
import {MatSidenav} from "@angular/material/sidenav";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css', './main-layout-background.css']
})
export class MainLayoutComponent {
  @ViewChild('sidenav', { static: false }) sidenav!: MatSidenav;
  public isAuthenticated!: Observable<boolean>;
  screenWidth!: number;
  resizeSubscription!: Subscription;

  constructor(private authService: AuthService, private breakpointObserver: BreakpointObserver) {
    this.isAuthenticated = this.authService.isAuthenticated;
  }

  ngOnInit(): void {
    this.resizeSubscription = this.breakpointObserver
      .observe([Breakpoints.Small, Breakpoints.HandsetPortrait])
      .subscribe((state) => {
        if (state.matches) {
          this.screenWidth = 0;
          if (this.sidenav != null) {
            this.sidenav.close();
          }
        } else {
          this.screenWidth = window.innerWidth;
          if (this.sidenav != null) {
            this.sidenav.open();
          }
        }
      });
  }

  ngOnDestroy(): void {
    this.resizeSubscription.unsubscribe();
  }

  logout() {
    this.authService.logout();
  }
}
