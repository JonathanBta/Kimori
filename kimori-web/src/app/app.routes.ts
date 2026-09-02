import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'sign-in',
    loadComponent: () => import('./features/auth/sign-in.component').then((m) => m.SignInComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/shell/app-shell.component').then((m) => m.AppShellComponent)
  },
  { path: '**', redirectTo: '' }
];
