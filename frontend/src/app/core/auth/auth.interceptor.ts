import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Adds `Authorization: Bearer <token>` to API calls, and logs the user out when the
 * backend says the token is no longer valid (401) - e.g. it expired or auth-service restarted.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const isApiCall = request.url.startsWith('/api/');
  const isAuthEndpoint = request.url.startsWith('/api/auth/');
  const token = isApiCall && !isAuthEndpoint ? auth.token() : null;

  const outgoing = token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      if (token && error instanceof HttpErrorResponse && error.status === 401) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
