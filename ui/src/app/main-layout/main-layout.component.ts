import {Component, ViewChild} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {AuthService} from "../services/auth.service";
import {MatSidenav} from "@angular/material/sidenav";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";
import {Router} from "@angular/router";
import {MatToolbar} from "@angular/material/toolbar";

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css', './main-layout-background.css']
})
export class MainLayoutComponent {
  @ViewChild('sidenav', { static: false }) sidenav!: MatSidenav;
  @ViewChild('footer', { static: false }) footer!: MatToolbar;
  public isAuthenticated!: Observable<boolean>;
  screenWidth!: number;
  resizeSubscription!: Subscription;

  constructor(private authService: AuthService, private breakpointObserver: BreakpointObserver,
              private router: Router) {
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
            if (this.footer != null) {
              this.footer._elementRef.nativeElement.style.display = 'none';
            }
          } else {
            this.screenWidth = window.innerWidth;
            if (this.sidenav != null) {
              this.sidenav.open();
            }
            if (this.footer != null) {
              this.footer._elementRef.nativeElement.style.display = 'block';
            }
          }
        });
  }

  ngOnDestroy(): void {
    this.resizeSubscription.unsubscribe();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['landing']);
  }
}
