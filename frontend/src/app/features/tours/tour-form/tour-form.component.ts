import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Tour } from '../../../core/models/tour.model';
import { TourApiService } from '../../../core/services/tour-api.service';

@Component({
  selector: 'app-tour-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './tour-form.component.html',
  styleUrl: './tour-form.component.css',
})
export class TourFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(TourApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

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
  /** gesetzt im Bearbeiten-Modus */
  protected tourId: number | null = null;

  ngOnInit(): void {
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
