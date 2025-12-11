import { Injectable, inject } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, switchMap, catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { LoggingService } from '../services/logging.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private auth = inject(AuthService);
  private logger = inject(LoggingService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.auth.getAccessToken();
    let authReq = req;
    if (token) {
      authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
  // log header for debugging
  this.logger.log('Sending request', { url: authReq.urlWithParams, authorization: authReq.headers.get('Authorization') });
  return next.handle(authReq).pipe(
      catchError((err) => {
        if (err instanceof HttpErrorResponse) {
          this.logger.error(`HTTP ${err.status} error for ${req.urlWithParams}`, err);
          if (err.status === 401) {
            this.logger.log('Received 401, attempting token refresh');
            return this.handle401(authReq, next);
          }
        }
        return throwError(() => err);
      })
    );
  }

  private handle401(req: HttpRequest<any>, next: HttpHandler) {
    this.logger.log('Refreshing token...');
    return this.auth.refreshToken().pipe(
      switchMap(res => {
        const newToken = res?.accessToken;
        if (!newToken) {
          this.logger.error('Refresh did not return a new access token', res);
          throw new Error('No new token');
        }
        this.logger.log('Token refreshed, retrying original request');
        const cloned = req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } });
        return next.handle(cloned);
      }),
      catchError(err => {
        this.logger.error('Token refresh failed, logging out', err);
        this.auth.logout();
        return throwError(() => err);
      })
    );
  }
}
