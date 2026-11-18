import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guards';

/** Pages are lazy-loaded: each route downloads its code only when first visited. */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  {
    path: 'login',
    title: 'Log in · Product Platform',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'signup',
    title: 'Sign up · Product Platform',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/signup-page').then((m) => m.SignupPage),
  },
  {
    path: 'products',
    title: 'Products · Product Platform',
    canActivate: [authGuard],
    loadComponent: () => import('./features/products/products-page').then((m) => m.ProductsPage),
  },
  { path: '**', redirectTo: 'products' },
];
