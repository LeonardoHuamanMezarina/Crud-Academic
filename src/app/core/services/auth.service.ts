import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { tap, Observable } from 'rxjs';
import { LoggingService } from './logging.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // En el backend los endpoints de auth están en /auth (no en /api/v1/auth)
  private api = `${environment.urlBackEnd}/auth`;
  private logger = inject(LoggingService);
  // Fallback en memoria para entornos donde sessionStorage/localStorage no existen
  private memoryStorage: Record<string, string> = {};
  constructor(private http: HttpClient) {
    this.logger.log('AuthService initialized, api=' + this.api);
  }

  login(username: string, password: string): Observable<any> {
    this.logger.log('Calling login endpoint', { url: `${this.api}/login`, username });
    return this.http.post<any>(`${this.api}/login`, { username, password }).pipe(
      tap(res => {
        this.logger.log('Login response', res);
        // soportar distintas formas de respuesta del backend
        const token = res?.bearer?.token ?? res?.accessToken ?? res?.token ?? res?.jwt;
        if (token) {
          this.safeSetSession('access_token', token);
        }
        // roles puede venir como 'roles' (array) o 'role' (string)
        if (res?.roles) {
          this.safeSetSession('roles', JSON.stringify(res.roles));
        } else if (res?.role) {
          this.safeSetSession('roles', JSON.stringify([res.role]));
        }
        if (res?.refreshToken) {
          this.safeSetLocal('refresh_token', res.refreshToken);
        }
      })
    );
  }

  register(payload: { username: string; email: string; password: string }) {
    this.logger.log('Calling register', { url: `${this.api}/register`, payload: { username: payload.username, email: payload.email } });
    return this.http.post<any>(`${this.api}/register`, payload);
  }

  logout() {
    this.safeRemoveSession('access_token');
    this.safeRemoveSession('roles');
    this.safeRemoveLocal('refresh_token');
  }

  getAccessToken() { return this.safeGetSession('access_token'); }
  getRefreshToken() { return this.safeGetLocal('refresh_token'); }

  refreshToken() {
    const refresh = this.getRefreshToken();
    this.logger.log('Calling refresh token endpoint', { url: `${this.api}/refresh` });
    return this.http.post<{ accessToken: string }>(`${this.api}/refresh`, { refreshToken: refresh }).pipe(
      tap(r => { this.logger.log('Refresh response', r); if (r?.accessToken) sessionStorage.setItem('access_token', r.accessToken); })
    );
  }

  getRoles(): string[] {
    const r = this.safeGetSession('roles');
    return r ? JSON.parse(r) : [];
  }

  // helper de depuración
  debugToken() {
    return this.getAccessToken();
  }

  // --- helpers para acceso seguro a storage ---
  private hasWindow(): boolean {
    try { return typeof window !== 'undefined' && !!window; } catch (e) { return false; }
  }

  private safeSetSession(key: string, value: string) {
    if (this.hasWindow() && typeof sessionStorage !== 'undefined') {
      try { sessionStorage.setItem(key, value); return; } catch (e) { /* fallthrough to memory */ }
    }
    this.memoryStorage[key] = value;
  }

  private safeGetSession(key: string): string | null {
    if (this.hasWindow() && typeof sessionStorage !== 'undefined') {
      try { return sessionStorage.getItem(key); } catch (e) { /* fallthrough */ }
    }
    return this.memoryStorage[key] ?? null;
  }

  private safeRemoveSession(key: string) {
    if (this.hasWindow() && typeof sessionStorage !== 'undefined') {
      try { sessionStorage.removeItem(key); return; } catch (e) { /* fallthrough */ }
    }
    delete this.memoryStorage[key];
  }

  private safeSetLocal(key: string, value: string) {
    if (this.hasWindow() && typeof localStorage !== 'undefined') {
      try { localStorage.setItem(key, value); return; } catch (e) { /* fallthrough to memory */ }
    }
    this.memoryStorage[key] = value;
  }

  private safeGetLocal(key: string): string | null {
    if (this.hasWindow() && typeof localStorage !== 'undefined') {
      try { return localStorage.getItem(key); } catch (e) { /* fallthrough */ }
    }
    return this.memoryStorage[key] ?? null;
  }

  private safeRemoveLocal(key: string) {
    if (this.hasWindow() && typeof localStorage !== 'undefined') {
      try { localStorage.removeItem(key); return; } catch (e) { /* fallthrough */ }
    }
    delete this.memoryStorage[key];
  }
}
