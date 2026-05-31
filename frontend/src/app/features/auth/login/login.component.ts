import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  mode = signal<AuthMode>('login');
  loading = signal(false);
  errorMessage = signal('');

  email = '';
  username = '';
  password = '';
  confirmPassword = '';

  setMode(m: AuthMode): void {
    this.mode.set(m);
    this.errorMessage.set('');
    this.email = '';
    this.username = '';
    this.password = '';
    this.confirmPassword = '';
  }

  submit(): void {
    this.errorMessage.set('');

    if (this.mode() === 'login') {
      this.doLogin();
    } else {
      this.doRegister();
    }
  }

  private doLogin(): void {
    if (!this.username || !this.password) {
      this.errorMessage.set('Bitte alle Felder ausfüllen.');
      return;
    }

    this.loading.set(true);
    this.auth.login({ username: this.username, password: this.password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.errorMessage.set(
          err.status === 401 ? 'Ungültige Zugangsdaten.' : 'Anmeldung fehlgeschlagen. Bitte erneut versuchen.'
        );
        this.loading.set(false);
      },
    });
  }

  private doRegister(): void {
    if (!this.username || !this.email || !this.password || !this.confirmPassword) {
      this.errorMessage.set('Bitte alle Felder ausfüllen.');
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage.set('Passwörter stimmen nicht überein.');
      return;
    }
    if (this.password.length < 6) {
      this.errorMessage.set('Passwort muss mindestens 6 Zeichen lang sein.');
      return;
    }

    this.loading.set(true);
    this.auth.register({ username: this.username, email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.errorMessage.set(
          err.status === 409 ? 'Benutzername oder E-Mail bereits vergeben.' : 'Registrierung fehlgeschlagen. Bitte erneut versuchen.'
        );
        this.loading.set(false);
      },
    });
  }
}
