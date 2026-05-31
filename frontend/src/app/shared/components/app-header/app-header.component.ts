import { Component, HostListener, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-app-header',
  imports: [RouterLink],
  templateUrl: './app-header.component.html',
  styleUrl: './app-header.component.css',
})
export class AppHeaderComponent {
  private readonly auth = inject(AuthService);

  /** Search text bound from parent (MVVM: view ↔ view-model state). */
  searchQuery = input<string>('');
  searchQueryChange = output<string>();

  dropdownOpen = signal(false);

  get username(): string {
    return this.auth.getUsername() ?? 'U';
  }

  get initial(): string {
    return this.username.charAt(0).toUpperCase();
  }

  toggleDropdown(): void {
    this.dropdownOpen.update((v) => !v);
  }

  logout(): void {
    this.auth.logout();
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchQueryChange.emit(value);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.dropdownOpen.set(false);
    }
  }
}
