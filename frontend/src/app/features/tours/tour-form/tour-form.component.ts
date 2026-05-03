import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  Observable,
  Subject,
  debounceTime,
  distinctUntilChanged,
  filter,
  finalize,
  of,
  switchMap,
} from 'rxjs';

import { Tour } from '../../../core/models/tour.model';
import { TourApiService } from '../../../core/services/tour-api.service';
import {
  GeocodingService,
  LocationSuggestion,
  RoutePreview,
} from '../../../core/services/geocoding.service';
import { TourRouteMapComponent } from '../../../shared/components/tour-route-map/tour-route-map.component';

interface RouteRequest {
  from: string;
  to: string;
  transport: string;
  fromCoords: { lon: number; lat: number } | null;
  toCoords: { lon: number; lat: number } | null;
}

@Component({
  selector: 'app-tour-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TourRouteMapComponent],
  templateUrl: './tour-form.component.html',
  styleUrl: './tour-form.component.css',
})
export class TourFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(TourApiService);
  private readonly geocoding = inject(GeocodingService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    description: [''],
    transportType: ['hike', Validators.required],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    distance: [0, [Validators.required, Validators.min(0)]],
    estimatedTime: [0, [Validators.required, Validators.min(0)]],
    imageUrl: [''],
    routeInfo: [''],
  });

  saving = false;
  errorMessage: string | null = null;
  routeErrorMessage: string | null = null;
  protected tourId: number | null = null;

  // Autocomplete state
  fromSuggestions: LocationSuggestion[] = [];
  toSuggestions: LocationSuggestion[] = [];
  showFromSuggestions = false;
  showToSuggestions = false;
  loadingFrom = false;
  loadingTo = false;
  loadingRoute = false;

  /** Koordinaten der zuletzt gewählten Suggestions – ermöglichen direkten Route-Call ohne Geocoding. */
  private fromCoords: { lon: number; lat: number } | null = null;
  private toCoords: { lon: number; lat: number } | null = null;

  private readonly fromSearch$ = new Subject<string>();
  private readonly toSearch$ = new Subject<string>();
  /** switchMap-Subject: jede neue Emission cancelt den laufenden Route-Request. */
  private readonly routePreview$ = new Subject<RouteRequest>();

  ngOnInit(): void {
    this.setupAutocomplete();
    this.setupRoutePreview();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && this.route.snapshot.url.some((s) => s.path === 'edit')) {
      this.tourId = Number(idParam);
      if (!Number.isFinite(this.tourId)) {
        this.router.navigate(['/']);
        return;
      }
      this.api.getTour(this.tourId).subscribe({
        next: (t) => this.patchFromTour(t),
        error: () => {
          this.errorMessage = 'Tour konnte nicht geladen werden.';
        },
      });
    }
  }

  private setupAutocomplete(): void {
    this.fromSearch$
      .pipe(
        debounceTime(350),
        distinctUntilChanged(),
        filter((q) => q.length >= 2),
        switchMap((q) => {
          this.loadingFrom = true;
          return this.geocoding
            .searchLocations(q)
            .pipe(finalize(() => (this.loadingFrom = false)));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((suggestions) => {
        this.fromSuggestions = suggestions;
        this.showFromSuggestions = suggestions.length > 0;
      });

    this.toSearch$
      .pipe(
        debounceTime(350),
        distinctUntilChanged(),
        filter((q) => q.length >= 2),
        switchMap((q) => {
          this.loadingTo = true;
          return this.geocoding
            .searchLocations(q)
            .pipe(finalize(() => (this.loadingTo = false)));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((suggestions) => {
        this.toSuggestions = suggestions;
        this.showToSuggestions = suggestions.length > 0;
      });
  }

  /**
   * switchMap cancels any running route request as soon as a new one arrives.
   * This prevents stale loading state from multiple rapid calls.
   */
  private setupRoutePreview(): void {
    this.routePreview$
      .pipe(
        switchMap((req) => {
          this.loadingRoute = true;
          this.routeErrorMessage = null;

          const call$: Observable<RoutePreview | null> =
            req.fromCoords && req.toCoords
              ? this.geocoding.previewRouteByCoords(
                  req.fromCoords.lon, req.fromCoords.lat,
                  req.toCoords.lon, req.toCoords.lat,
                  req.transport,
                )
              : this.geocoding.previewRoute(req.from, req.to, req.transport);

          return call$.pipe(
            finalize(() => {
              this.loadingRoute = false;
              this.cdr.detectChanges();
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (preview) => {
          if (preview) {
            this.routeErrorMessage = null;
            this.form.patchValue({
              distance: Math.round(preview.distance * 10) / 10,
              estimatedTime: Math.round(preview.estimatedTime * 100) / 100,
              routeInfo: preview.routeInfo,
            });
          } else {
            this.routeErrorMessage = 'Route konnte nicht berechnet werden.';
          }
        },
        error: () => {
          this.loadingRoute = false;
          this.routeErrorMessage = 'Fehler bei der Routenberechnung.';
        },
      });
  }

  private patchFromTour(t: Tour): void {
    this.form.patchValue({
      name: t.name,
      description: t.description ?? '',
      transportType: (t.transportType || 'hike').toLowerCase(),
      fromLocation: t.fromLocation,
      toLocation: t.toLocation,
      distance: t.distance,
      estimatedTime: t.estimatedTime,
      imageUrl: t.imageUrl ?? '',
      routeInfo: t.routeInfo ?? '',
    });
  }

  // --- Autocomplete event handlers ---

  onFromInput(event: Event): void {
    const q = (event.target as HTMLInputElement).value;
    this.fromCoords = null;
    if (!q || q.length < 2) {
      this.fromSuggestions = [];
      this.showFromSuggestions = false;
      return;
    }
    this.showFromSuggestions = true;
    this.fromSearch$.next(q);
  }

  onToInput(event: Event): void {
    const q = (event.target as HTMLInputElement).value;
    this.toCoords = null;
    if (!q || q.length < 2) {
      this.toSuggestions = [];
      this.showToSuggestions = false;
      return;
    }
    this.showToSuggestions = true;
    this.toSearch$.next(q);
  }

  selectFrom(s: LocationSuggestion): void {
    this.form.controls.fromLocation.setValue(s.label);
    this.fromCoords = { lon: s.lon, lat: s.lat };
    this.fromSuggestions = [];
    this.showFromSuggestions = false;
    this.triggerRoutePreview();
  }

  selectTo(s: LocationSuggestion): void {
    this.form.controls.toLocation.setValue(s.label);
    this.toCoords = { lon: s.lon, lat: s.lat };
    this.toSuggestions = [];
    this.showToSuggestions = false;
    this.triggerRoutePreview();
  }

  hideFromSuggestions(): void {
    setTimeout(() => { this.showFromSuggestions = false; }, 150);
  }

  hideToSuggestions(): void {
    setTimeout(() => { this.showToSuggestions = false; }, 150);
  }

  // --- Route preview ---

  private triggerRoutePreview(): void {
    const from = this.form.controls.fromLocation.value?.trim();
    const to = this.form.controls.toLocation.value?.trim();
    if (!from || !to) return;

    this.routePreview$.next({
      from,
      to,
      transport: this.form.controls.transportType.value,
      fromCoords: this.fromCoords,
      toCoords: this.toCoords,
    });
  }

  /** Called when transport type button is clicked – re-fetch route if both locations are set. */
  setTransportType(value: string): void {
    this.form.controls.transportType.setValue(value);
    // Clear routeInfo since transport changed (old route no longer valid)
    this.form.controls.routeInfo.setValue('');
    this.triggerRoutePreview();
  }

  /** GeoJSON for map preview */
  protected routeGeoJsonForMap(): string | null {
    const v = this.form.controls.routeInfo.getRawValue()?.trim();
    return v || null;
  }

  cancel(): void {
    void this.router.navigate(['/']);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const payload: Tour = {
      name: v.name.trim(),
      description: v.description.trim(),
      transportType: v.transportType,
      fromLocation: v.fromLocation.trim(),
      toLocation: v.toLocation.trim(),
      distance: v.distance,
      estimatedTime: v.estimatedTime,
      imageUrl: v.imageUrl.trim() || null,
      routeInfo: v.routeInfo.trim() || null,
    };

    this.saving = true;
    this.errorMessage = null;

    const req =
      this.tourId == null
        ? this.api.createTour(payload)
        : this.api.updateTour(this.tourId, { ...payload, id: this.tourId });

    req
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => void this.router.navigate(['/']),
        error: () => {
          this.errorMessage = 'Speichern fehlgeschlagen.';
        },
      });
  }
}
