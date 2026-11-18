import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthResponse } from './auth.models';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const response: AuthResponse = {
    accessToken: 'header.payload.signature',
    tokenType: 'Bearer',
    expiresIn: 3600,
    user: { id: 'u1', email: 'ada@example.com', fullName: 'Ada', role: 'USER', createdAt: '2026-01-01T00:00:00Z' },
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      // logout() navigates to /login, so the test router needs that route.
      providers: [provideRouter([{ path: 'login', children: [] }]), provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('stores the session after a successful login', () => {
    service.login({ email: 'ada@example.com', password: 'secret123' }).subscribe();

    const request = http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush(response);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.user()?.fullName).toBe('Ada');
    expect(service.token()).toBe('header.payload.signature');
  });

  it('clears the session on logout', () => {
    service.login({ email: 'ada@example.com', password: 'secret123' }).subscribe();
    http.expectOne('/api/auth/login').flush(response);

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.token()).toBeNull();
    expect(localStorage.length).toBe(0);
  });
});
