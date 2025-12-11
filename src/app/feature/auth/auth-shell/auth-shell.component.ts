import { Component } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: true,
  selector: 'app-auth-shell',
  imports: [CommonModule, NgIf, FormsModule, RouterModule],
  templateUrl: './auth-shell.component.html',
  styleUrls: ['./auth-shell.component.scss']
})
export class AuthShellComponent {
  showRegister = false;

  // estado para formularios
  username = '';
  password = '';
  email = '';
  loading = false;
  error = '';

  constructor(private router: Router, private auth: AuthService) {}

  toggle(to?: 'login' | 'register') {
    if (to === 'register') this.showRegister = true;
    else if (to === 'login') this.showRegister = false;
    else this.showRegister = !this.showRegister;
  }

  goTo(path: string) {
    this.router.navigate([path]);
  }

  submitLogin() {
    this.loading = true; this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => { this.loading = false; this.router.navigate(['/customer-list']); },
      error: () => { this.loading = false; this.error = 'Credenciales inválidas'; }
    });
  }

  submitRegister() {
    this.loading = true; this.error = '';
    this.auth.register({ username: this.username, email: this.email, password: this.password }).subscribe({
      next: () => { this.loading = false; this.showRegister = false; },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Error al registrar'; }
    });
  }
}
