import { Injectable, inject } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, catchError } from 'rxjs';
import { LoggingService } from '../services/logging.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private logger = inject(LoggingService);
  private snack = inject(MatSnackBar);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err) => {
        if (err instanceof HttpErrorResponse) {
          const msg = err.error?.message || err.message || `Error HTTP ${err.status}`;
          this.logger.error('HTTP Error:', { url: req.urlWithParams, status: err.status, message: msg });
          // show friendly message for common errors
          if (err.status >= 400 && err.status < 600) {
            try { this.snack.open(msg, 'Cerrar', { duration: 5000 }); } catch (e) { /* ignore */ }
          }
        } else {
          this.logger.error('Non-HTTP error intercepted', err);
        }
        return throwError(() => err);
      })
    );
  }
}
