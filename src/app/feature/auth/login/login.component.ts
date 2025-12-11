import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading = true;
    this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        // Forzar la navegación al siguiente ciclo de eventos de JS.
        // Esto da tiempo a que el token se guarde en la sesión antes de que los guards/resolvers de la nueva ruta se disparen.
        setTimeout(() => this.router.navigate(['/customer-list']), 0);
      },
      error: (err) => { this.loading = false; this.error = 'Credenciales inválidas'; }
    });
  }

  goRegister() {
    this.router.navigate(['/register']);
  }
}
