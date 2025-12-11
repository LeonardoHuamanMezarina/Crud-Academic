import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const roles = this.auth.getRoles();
    const expected = route.data['roles'] as string[] || [];
    const ok = expected.length === 0 || expected.some(r => roles.includes(r));
    if (!ok) {
      this.router.navigate(['/login']);
      return false;
    }
    return true;
  }
}
