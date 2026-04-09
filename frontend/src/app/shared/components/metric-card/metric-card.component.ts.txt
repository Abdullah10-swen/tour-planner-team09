import { Component, input } from '@angular/core';

/** Small stat box (reusable UI component). */
@Component({
  selector: 'app-metric-card',
  templateUrl: './metric-card.component.html',
  styleUrl: './metric-card.component.css',
})
export class MetricCardComponent {
  label = input.required<string>();
  value = input.required<string>();
}
