import {AfterViewInit, ChangeDetectorRef, Component, ViewChild} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {AuthService} from "../services/auth.service";
import {MatSidenav} from "@angular/material/sidenav";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";
import {Router} from "@angular/router";
import {MatToolbar} from "@angular/material/toolbar";
import { Renderer2 } from '@angular/core';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css', './main-layout-background.css']
})
export class MainLayoutComponent implements AfterViewInit {
  @ViewChild('sidenav', { static: false }) sidenav!: MatSidenav;
    @ViewChild('footer', { static: false }) footer!: MatToolbar;
  @ViewChild('header', { static: false }) header!: MatToolbar;
  public isAuthenticated!: Observable<boolean>;
  screenWidth!: number;
  resizeSubscription!: Subscription;
  isMobile: boolean = false;
  lastScrollTop = 0;
  isHeaderVisible = true;

  constructor(private authService: AuthService, private breakpointObserver: BreakpointObserver,
              private router: Router, private renderer: Renderer2, private cdRef: ChangeDetectorRef) {
    this.isAuthenticated = this.authService.isAuthenticated;
  }

  ngOnInit(): void {
    window.addEventListener('scroll', this.handleScroll.bind(this));
  }

  ngAfterViewInit(): void {
    this.hideHeaderOnScroll(); // This should ensure header is available when called

    this.resizeSubscription = this.breakpointObserver
        .observe([Breakpoints.Small, Breakpoints.HandsetPortrait])
        .subscribe((state) => {
          if (state.matches) {
            this.isMobile = true;
            if (this.sidenav) {
              this.sidenav.close();
            }
            if (this.footer) {
              this.renderer.setStyle(this.footer._elementRef.nativeElement, 'display', 'none');
            }
          } else {
            this.isMobile = false;
            if (this.sidenav) {
              this.sidenav.open();
            }
            if (this.footer) {
              this.renderer.setStyle(this.footer._elementRef.nativeElement, 'display', 'block');
            }
            if (this.header) {
              this.renderer.setStyle(this.header._elementRef.nativeElement, 'display', 'block');
            }
          }
        });

    this.cdRef.detectChanges();
  }



  handleScroll() {
    const currentScrollTop = window.scrollY;

    // Scrolling down
    if (currentScrollTop > this.lastScrollTop) {
      this.isHeaderVisible = false;
      if (this.isMobile) {
        this.renderer.setStyle(this.header._elementRef.nativeElement, 'display', 'none'); // Adjusted line
      }
    }
    // Scrolling up or reaching the top of the page
    else {
      this.isHeaderVisible = true;
      if (this.isMobile) {
        this.renderer.setStyle(this.header._elementRef.nativeElement, 'display', 'block'); // Adjusted line
      }
    }

    this.lastScrollTop = currentScrollTop;
  }


  hideHeaderOnScroll(): void {
    if (this.header && this.isMobile) { // Check if header exists before proceeding
      let lastScrollTop = 0;
      document.addEventListener('scroll', () => {
        const currentScrollTop = window.scrollY || document.documentElement.scrollTop;
        if (currentScrollTop === 0 || currentScrollTop < lastScrollTop) {
          this.renderer.setStyle(this.header._elementRef.nativeElement, 'display', 'block');
        } else {
          this.renderer.setStyle(this.header._elementRef.nativeElement, 'display', 'none');
        }
        lastScrollTop = currentScrollTop;
      });
    }
  }




  ngOnDestroy(): void {
    this.resizeSubscription.unsubscribe();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['landing']);
  }
}
