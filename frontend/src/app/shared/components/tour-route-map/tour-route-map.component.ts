import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-tour-route-map',
  templateUrl: './tour-route-map.component.html',
  styleUrl: './tour-route-map.component.css',
})
export class TourRouteMapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() routeGeoJson: string | null = null;

  @ViewChild('mapEl', { static: true })
  mapElRef!: ElementRef<HTMLDivElement>;

  private map: L.Map | null = null;
  private routeLayer: L.GeoJSON | null = null;

  ngAfterViewInit(): void {
    this.map = L.map(this.mapElRef.nativeElement).setView([47.5, 14.0], 7);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
    }).addTo(this.map);
    // Give the browser a tick to finish layout so Leaflet calculates dimensions correctly
    setTimeout(() => {
      this.map?.invalidateSize();
      this.renderRoute();
    }, 0);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['routeGeoJson'] && this.map) {
      this.renderRoute();
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
    this.map = null;
  }

  private renderRoute(): void {
    if (!this.map) return;
    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
      this.routeLayer = null;
    }
    if (!this.routeGeoJson) {
      this.map.setView([47.5, 14.0], 7);
      return;
    }
    try {
      const data = JSON.parse(this.routeGeoJson) as GeoJSON.GeoJsonObject;
      this.routeLayer = L.geoJSON(data, {
        style: { color: '#1b4332', weight: 5, opacity: 0.85 },
      }).addTo(this.map);
      const bounds = this.routeLayer.getBounds();
      if (bounds.isValid()) {
        this.map.fitBounds(bounds, { padding: [30, 30] });
      }
    } catch {
      // invalid JSON – keep current view
    }
  }
}
