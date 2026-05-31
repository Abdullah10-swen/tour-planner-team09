import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tours/tour-dashboard/tour-dashboard.component').then(
        (m) => m.TourDashboardComponent
      ),
  },
  {
    path: 'tours/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tours/tour-form/tour-form.component').then(
        (m) => m.TourFormComponent
      ),
  },
  {
    path: 'tours/:tourId/logs/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tours/tour-log-form/tour-log-form.component').then(
        (m) => m.TourLogFormComponent
      ),
  },
  {
    path: 'tours/:tourId/logs/:logId/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tours/tour-log-form/tour-log-form.component').then(
        (m) => m.TourLogFormComponent
      ),
  },
  {
    path: 'tours/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tours/tour-form/tour-form.component').then(
        (m) => m.TourFormComponent
      ),
  },
  { path: '**', redirectTo: '' },
];
