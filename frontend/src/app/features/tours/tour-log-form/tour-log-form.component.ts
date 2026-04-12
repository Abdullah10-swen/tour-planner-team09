import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';

import { Tour } from '../../../core/models/tour.model';
import { TourLog } from '../../../core/models/tour-log.model';
import { TourApiService } from '../../../core/services/tour-api.service';

@Component({
  selector: 'app-tour-log-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './tour-log-form.component.html',
  styleUrl: './tour-log-form.component.css',
})
export class TourLogFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(TourApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  /** Signal, damit die View nach HTTP auch ohne Zone.js aktualisiert wird. */
  readonly tour = signal<Tour | null>(null);
  tourId = 0;
  /** gesetzt auf Log-ID im Bearbeiten-Modus */
  protected logId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    dateTime: ['', Validators.required],
    comment: ['', [Validators.required, Validators.maxLength(2000)]],
    difficulty: [2, [Validators.required, Validators.min(1), Validators.max(3)]],
    totalDistance: [0, [Validators.required, Validators.min(0)]],
    totalTime: [1, [Validators.required, Validators.min(0)]],
    rating: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
  });

  saving = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    const raw = this.route.snapshot.paramMap.get('tourId');
    const id = Number(raw);
    if (!Number.isFinite(id)) {
      void this.router.navigate(['/']);
      return;
    }
    this.tourId = id;

    const logParam = this.route.snapshot.paramMap.get('logId');
    const isEdit =
      logParam != null &&
      this.route.snapshot.url.some((s) => s.path === 'edit');

    if (isEdit) {
      const lid = Number(logParam);
      if (!Number.isFinite(lid)) {
        void this.router.navigate(['/']);
        return;
      }
      this.logId = lid;
      forkJoin({
        tour: this.api.getTour(id),
        log: this.api.getLog(id, lid),
      }).subscribe({
        next: ({ tour, log }) => {
          this.tour.set(tour);
          this.patchFromLog(log);
        },
        error: () => {
          this.errorMessage = 'Tour oder Log konnte nicht geladen werden.';
        },
      });
      return;
    }

    this.api.getTour(id).subscribe({
      next: (t) => this.tour.set(t),
      error: () => {
        this.errorMessage = 'Tour nicht gefunden.';
      },
    });

    const now = new Date();
    const pad = (n: number) => n.toString().padStart(2, '0');
    const local = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
    this.form.controls.dateTime.setValue(local);
  }

  private patchFromLog(log: TourLog): void {
    if (log.dateTime) {
      this.form.controls.dateTime.setValue(this.isoToDatetimeLocal(log.dateTime));
    }
    this.form.patchValue({
      comment: log.comment ?? '',
      difficulty: log.difficulty,
      totalDistance: log.totalDistance,
      totalTime: log.totalTime,
      rating: log.rating,
    });
  }

  /** ISO-String (vom Backend) → Wert für input[type=datetime-local] */
  private isoToDatetimeLocal(iso: string): string {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) {
      return '';
    }
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  setDifficulty(value: number): void {
    this.form.controls.difficulty.setValue(value);
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
    const iso = this.toIsoDateTime(v.dateTime);
    const payload: TourLog = {
      comment: v.comment.trim(),
      difficulty: v.difficulty,
      totalDistance: v.totalDistance,
      totalTime: v.totalTime,
      rating: v.rating,
      dateTime: iso,
    };

    this.saving = true;
    this.errorMessage = null;

    const req =
      this.logId == null
        ? this.api.createLog(this.tourId, payload)
        : this.api.updateLog(this.tourId, this.logId, {
            ...payload,
            id: this.logId,
            tourId: this.tourId,
          });

    req.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => void this.router.navigate(['/']),
      error: () => {
        this.errorMessage = 'Log konnte nicht gespeichert werden.';
      },
    });
  }

  private toIsoDateTime(local: string): string {
    if (!local) {
      return new Date().toISOString();
    }
    const withSeconds = local.length === 16 ? `${local}:00` : local;
    return new Date(withSeconds).toISOString();
  }
}
