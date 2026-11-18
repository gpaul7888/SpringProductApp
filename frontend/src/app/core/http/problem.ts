import { HttpErrorResponse } from '@angular/common/http';

/** RFC 9457 problem body, as returned by every backend service. */
export interface ProblemDetail {
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
}

/** Turns any HTTP error into one human-readable sentence for the UI. */
export function errorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }
  if (error.status === 0) {
    return 'Cannot reach the server. Is the backend running?';
  }
  const problem = error.error as ProblemDetail | null;
  if (problem?.errors && Object.keys(problem.errors).length > 0) {
    return Object.values(problem.errors).join(' ');
  }
  if (problem?.detail) {
    return problem.detail;
  }
  if (error.status === 503 || error.status === 502 || error.status === 504) {
    return 'A backend service is starting or unavailable. Please try again in a few seconds.';
  }
  return fallback;
}
