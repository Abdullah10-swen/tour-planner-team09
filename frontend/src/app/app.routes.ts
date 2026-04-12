import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/tours/tour-dashboard/tour-dashboard.component').then(
        (m) => m.TourDashboardComponent
      ),
  },
  {
    path: 'tours/new',
    loadComponent: () =>
      import('./features/tours/tour-form/tour-form.component').then(
        (m) => m.TourFormComponent
      ),
  },
  {
    path: 'tours/:tourId/logs/new',
    loadComponent: () =>
      import('./features/tours/tour-log-form/tour-log-form.component').then(
        (m) => m.TourLogFormComponent
      ),
  },
  {
    path: 'tours/:tourId/logs/:logId/edit',
    loadComponent: () =>
      import('./features/tours/tour-log-form/tour-log-form.component').then(
        (m) => m.TourLogFormComponent
      ),
  },
  {
    path: 'tours/:id/edit',
    loadComponent: () =>
      import('./features/tours/tour-form/tour-form.component').then(
        (m) => m.TourFormComponent
      ),
  },
  { path: '**', redirectTo: '' },
];
