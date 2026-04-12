import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api/api-base';
import { Tour } from '../models/tour.model';
import { TourLog } from '../models/tour-log.model';

@Injectable({ providedIn: 'root' })
export class TourApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE_URL}/tours`;

  getTours(): Observable<Tour[]> {
    return this.http.get<Tour[]>(this.base);
  }

  getTour(id: number): Observable<Tour> {
    return this.http.get<Tour>(`${this.base}/${id}`);
  }

  createTour(tour: Tour): Observable<Tour> {
    return this.http.post<Tour>(this.base, tour);
  }

  updateTour(id: number, tour: Tour): Observable<Tour> {
    return this.http.put<Tour>(`${this.base}/${id}`, tour);
  }

  deleteTour(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  getLogs(tourId: number): Observable<TourLog[]> {
    return this.http.get<TourLog[]>(`${this.base}/${tourId}/logs`);
  }

  getLog(tourId: number, logId: number): Observable<TourLog> {
    return this.http.get<TourLog>(`${this.base}/${tourId}/logs/${logId}`);
  }

  createLog(tourId: number, log: TourLog): Observable<TourLog> {
    return this.http.post<TourLog>(`${this.base}/${tourId}/logs`, log);
  }

  updateLog(tourId: number, logId: number, log: TourLog): Observable<TourLog> {
    return this.http.put<TourLog>(`${this.base}/${tourId}/logs/${logId}`, log);
  }

  deleteLog(tourId: number, logId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${tourId}/logs/${logId}`);
  }
}
