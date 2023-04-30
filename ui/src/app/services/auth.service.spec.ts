import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User } from '../models/emotion.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('signUp', () => {
    it('should send a post request to the server', () => {
      const user: User = {username: 'testuser', password: 'testpassword', email: 'a@b.c'};
      service.signUp(user).subscribe((response) => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne('http://localhost:4200/api/user');
      expect(req.request.method).toEqual('POST');
      req.flush({});
    });
  });

  describe('login', () => {
    it('should send a post request to the server', () => {
      const username = 'testuser';
      const password = 'testpassword';
      service.login(username, password).subscribe((response) => {
        expect(response.token).toBeTruthy();
      });

      const req = httpMock.expectOne('http://localhost:4200/api/login');
      expect(req.request.method).toEqual('POST');
      req.flush({ token: 'testtoken' });

      expect(localStorage.getItem('auth_token')).toEqual('testtoken');
    });

    it('should handle error response', () => {
      const username = 'testuser';
      const password = 'testpassword';
      const out = service.login(username, password).subscribe(
        {
          next: (isAuthenticated) => {
            expect(isAuthenticated).toBeTrue();
          },
          error: (error) => {
            expect(error).toBeTruthy();
          }
        }
      );

      const req = httpMock.expectOne('http://localhost:4200/api/login');
      expect(req.request.method).toEqual('POST');
      req.flush({}, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('logout', () => {
    it('should remove auth_token from localStorage and set isAuthenticatedSubject to false', () => {
      localStorage.setItem('auth_token', 'testtoken');
      service.isAuthenticatedSubject.next(true);

      service.logout();

      expect(localStorage.getItem('auth_token')).toBeFalsy();
      service.isAuthenticated.subscribe((isAuthenticated) => {
        expect(isAuthenticated).toBeFalsy();
      });
    });
  });

  describe('getAuthorizationHeader', () => {
    it('should return HttpHeaders object with Authorization header when auth_token is available in localStorage', () => {
      localStorage.setItem('auth_token', 'testtoken');

      const headers = service.getAuthorizationHeader();

      expect(headers.get('Authorization')).toEqual('Bearer testtoken');
    });

    it('should return HttpHeaders object without Authorization header when auth_token is not available in localStorage', () => {
      localStorage.removeItem('auth_token');

      const headers = service.getAuthorizationHeader();

      expect(headers.get('Authorization')).toBeFalsy();
    });
  });

  it('should decode the token', () => {
    localStorage.setItem('auth_token', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c');
    const decodedToken = service.fetchDecodedToken();
    expect(decodedToken.sub).toEqual('1234567890');
    expect(decodedToken.name).toEqual('John Doe');
  });

  afterEach(() => {
    localStorage.removeItem('token');
  });
});
