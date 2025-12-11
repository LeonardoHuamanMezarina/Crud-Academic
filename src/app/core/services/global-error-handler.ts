import { ErrorHandler, Injectable, inject } from '@angular/core';
import { LoggingService } from './logging.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private logger = inject(LoggingService);
  private snack = inject(MatSnackBar);

  handleError(error: any): void {
    // Log detailed error for debugging/remote logging
    this.logger.error('Global error handler caught error', error);
    // Show user friendly message
    try {
      const msg = (error && error.message) ? error.message : 'Ocurrió un error inesperado';
      this.snack.open(msg, 'Cerrar', { duration: 5000 });
    } catch (e) {
      // ignore snack errors
    }
    // opcional: enviar a un endpoint de logging remoto
    // fetch('/api/logs', { method: 'POST', body: JSON.stringify({ error }) })
    // Re-throw si quieres que Angular siga con su log por consola
    // throw error;
  }
}
