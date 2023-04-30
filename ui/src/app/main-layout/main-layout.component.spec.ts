import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MainLayoutComponent } from './main-layout.component';
import { AuthService } from '../services/auth.service';
import { RouterTestingModule } from '@angular/router/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatSidenavModule } from '@angular/material/sidenav';
import { of } from 'rxjs';

describe('MainLayoutComponent', () => {
  let component: MainLayoutComponent;
  let fixture: ComponentFixture<MainLayoutComponent>;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        MatButtonModule,
        MatToolbarModule,
        BrowserAnimationsModule,
        MatSidenavModule
      ],
      declarations: [MainLayoutComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: of(false),
            logout: jasmine.createSpy('logout')
          }
        }
      ],
    }).compileComponents();

    authService = TestBed.inject(AuthService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MainLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call AuthService.logout when clicking the Sign out button', () => {
    component.logout();
    expect(authService.logout).toHaveBeenCalled();
  });

  it('should display Login and Sign up buttons when not authenticated', () => {
    authService.isAuthenticated = of(false);
    fixture.detectChanges();
    const loginButton = fixture.nativeElement.querySelector('button[routerLink="/login"]');
    const signupButton = fixture.nativeElement.querySelector('button[routerLink="/signup"]');
    expect(loginButton).toBeTruthy();
    expect(signupButton).toBeTruthy();
  });

  it('should display Sign out button when authenticated', () => {
    authService.isAuthenticated = of(true);
    fixture.detectChanges();
    const signOutButton = fixture.nativeElement.querySelector('button[mat-stroked-button]');
    expect(signOutButton).toBeTruthy();
  });
});
