import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';

import { API_BASE_URL } from '../api/api-base';

export interface LocationSuggestion {
  label: string;
  lon: number;
  lat: number;
}

export interface RoutePreview {
  distance: number;
  estimatedTime: number;
  routeInfo: string;
}

@Injectable({ providedIn: 'root' })
export class GeocodingService {
  private readonly http = inject(HttpClient);

  searchLocations(q: string): Observable<LocationSuggestion[]> {
    if (!q || q.trim().length < 2) return of([]);
    return this.http
      .get<LocationSuggestion[]>(`${API_BASE_URL}/geocode/search`, {
        params: { q: q.trim() },
      })
      .pipe(
        timeout(8000),
        catchError(() => of([])),
      );
  }

  /**
   * Schnelle Variante: Koordinaten aus dem Autocomplete-Dropdown übergeben.
   * Spart zwei Geocoding-Calls → nur ein HTTP-Request zum Backend.
   */
  previewRouteByCoords(
    fromLon: number, fromLat: number,
    toLon: number, toLat: number,
    transport: string,
  ): Observable<RoutePreview | null> {
    return this.http
      .get<RoutePreview>(`${API_BASE_URL}/route-preview`, {
        params: { fromLon, fromLat, toLon, toLat, transport },
      })
      .pipe(
        timeout(15000),
        catchError(() => of(null)),
      );
  }

  /**
   * Fallback: Adressen als Freitext (wenn keine Koordinaten bekannt sind).
   */
  previewRoute(from: string, to: string, transport: string): Observable<RoutePreview | null> {
    return this.http
      .get<RoutePreview>(`${API_BASE_URL}/route-preview`, {
        params: { from, to, transport },
      })
      .pipe(
        timeout(15000),
        catchError(() => of(null)),
      );
  }
}
