import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { AuthResponse, AuthSession, LoginRequest, SignupRequest, User } from './auth.models';

const STORAGE_KEY = 'product-platform.session';

/**
 * Holds the logged-in session as a signal. Components read `user()` / `isAuthenticated()`
 * and re-render automatically when it changes.
 *
 * Note: the token lives in localStorage for simplicity. Production apps often prefer an
 * HttpOnly cookie set by a backend-for-frontend, which page scripts cannot read.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly session = signal<AuthSession | null>(this.restore());

  readonly user = computed<User | null>(() => this.session()?.user ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);

  /** Current token, or null when logged out or expired. */
  token(): string | null {
    const current = this.session();
    if (current && current.expiresAt <= Date.now()) {
      this.clear();
      return null;
    }
    return current?.accessToken ?? null;
  }

  login(request: LoginRequest): Observable<User> {
    return this.http.post<AuthResponse>('/api/auth/login', request).pipe(
      tap((response) => this.store(response)),
      map((response) => response.user),
    );
  }

  signup(request: SignupRequest): Observable<User> {
    return this.http.post<AuthResponse>('/api/auth/signup', request).pipe(
      tap((response) => this.store(response)),
      map((response) => response.user),
    );
  }

  logout(): void {
    this.clear();
    void this.router.navigate(['/login']);
  }

  private store(response: AuthResponse): void {
    const session: AuthSession = {
      accessToken: response.accessToken,
      expiresAt: Date.now() + response.expiresIn * 1000,
      user: response.user,
    };
    this.session.set(session);
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    }
  }

  private clear(): void {
    this.session.set(null);
    if (this.isBrowser) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  private restore(): AuthSession | null {
    if (!this.isBrowser) {
      return null;
    }
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      const session = raw ? (JSON.parse(raw) as AuthSession) : null;
      return session && session.expiresAt > Date.now() ? session : null;
    } catch {
      return null;
    }
  }
}
