/** Mirrors auth-service's UserResponse record. */
export interface User {
  id: string;
  email: string;
  fullName: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

/** Mirrors auth-service's AuthResponse record. */
export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest extends LoginRequest {
  fullName: string;
}

/** What the browser keeps between page loads. */
export interface AuthSession {
  accessToken: string;
  expiresAt: number;
  user: User;
}
