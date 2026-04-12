import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, finalize, of } from 'rxjs';

import { Tour } from '../../../core/models/tour.model';
import { TourLog } from '../../../core/models/tour-log.model';
import { TourApiService } from '../../../core/services/tour-api.service';
import { AppHeaderComponent } from '../../../shared/components/app-header/app-header.component';
import { MetricCardComponent } from '../../../shared/components/metric-card/metric-card.component';

type TransportFilter = 'all' | 'bike' | 'hike' | 'run' | 'vacation';

const TRANSPORT_OPTIONS: { id: TransportFilter; label: string }[] = [
  { id: 'all', label: 'Alle' },
  { id: 'bike', label: 'Bike' },
  { id: 'hike', label: 'Hike' },
  { id: 'run', label: 'Run' },
  { id: 'vacation', label: 'Vacation' },
];

@Component({
  selector: 'app-tour-dashboard',
  imports: [CommonModule, RouterLink, AppHeaderComponent, MetricCardComponent],
  templateUrl: './tour-dashboard.component.html',
  styleUrl: './tour-dashboard.component.css',
})
export class TourDashboardComponent {
  private readonly api = inject(TourApiService);

  readonly transportOptions = TRANSPORT_OPTIONS;

  readonly tours = signal<Tour[]>([]);
  readonly selectedTourId = signal<number | null>(null);
  readonly logs = signal<TourLog[]>([]);
  readonly transportFilter = signal<TransportFilter>('all');
  readonly searchQuery = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly selectedTour = computed(() => {
    const id = this.selectedTourId();
    if (id == null) {
      return null;
    }
    return this.tours().find((t) => t.id === id) ?? null;
  });

  readonly filteredTours = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    const f = this.transportFilter();
    return this.tours().filter((t) => {
      if (f !== 'all' && t.transportType.toLowerCase() !== f) {
        return false;
      }
      if (!q) {
        return true;
      }
      const hay = [
        t.name,
        t.description,
        t.fromLocation,
        t.toLocation,
        t.transportType,
      ]
        .join(' ')
        .toLowerCase();
      return hay.includes(q);
    });
  });

  constructor() {
    this.refreshTours();
  }

  refreshTours(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api
      .getTours()
      .pipe(
        catchError(() => {
          this.errorMessage.set(
            'Backend nicht erreichbar. Starte Spring Boot auf http://localhost:8080 .'
          );
          return of([] as Tour[]);
        }),
        finalize(() => this.loading.set(false))
      )
      .subscribe((list) => {
        this.tours.set(list);
        const sel = this.selectedTourId();
        if (sel != null && !list.some((t) => t.id === sel)) {
          this.selectedTourId.set(null);
          this.logs.set([]);
        }
      });
  }

  selectTour(tour: Tour): void {
    if (tour.id == null) {
      return;
    }
    this.selectedTourId.set(tour.id);
    this.api.getLogs(tour.id).subscribe({
      next: (l) => this.logs.set(l),
      error: () => this.logs.set([]),
    });
  }

  setFilter(value: TransportFilter): void {
    this.transportFilter.set(value);
  }

  onSearchChange(value: string): void {
    this.searchQuery.set(value);
  }

  deleteLog(tourId: number, logId: number | undefined): void {
    if (logId == null) {
      return;
    }
    if (!confirm('Diesen Log wirklich löschen?')) {
      return;
    }
    this.api.deleteLog(tourId, logId).subscribe({
      next: () => {
        this.api.getLogs(tourId).subscribe({
          next: (l) => this.logs.set(l),
        });
      },
      error: () =>
        this.errorMessage.set('Log konnte nicht gelöscht werden.'),
    });
  }

  deleteSelected(): void {
    const id = this.selectedTourId();
    if (id == null) {
      return;
    }
    if (!confirm('Diese Tour wirklich löschen?')) {
      return;
    }
    this.api.deleteTour(id).subscribe({
      next: () => {
        this.selectedTourId.set(null);
        this.logs.set([]);
        this.refreshTours();
      },
      error: () =>
        this.errorMessage.set('Tour konnte nicht gelöscht werden.'),
    });
  }

  formatKm(km: number): string {
    return `${km.toLocaleString('de-AT', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} km`;
  }

  formatHours(h: number): string {
    const totalMin = Math.round(h * 60);
    const hh = Math.floor(totalMin / 60);
    const mm = totalMin % 60;
    if (hh === 0) {
      return `${mm} min`;
    }
    return `${hh}h ${mm.toString().padStart(2, '0')} min`;
  }

  logDateLabel(iso: string | null): string {
    if (!iso) {
      return '—';
    }
    const d = new Date(iso);
    return d.toLocaleDateString('de-AT', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  }
}
