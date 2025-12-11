import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LoggingService } from '../services/logging.service';

/**
 * Centralized error controller to show user-friendly messages
 * and to centralize common validation errors across the app.
 */
@Injectable({ providedIn: 'root' })
export class ErrorControllerService {
  private snack = inject(MatSnackBar);
  private logger = inject(LoggingService);

  showError(message: string, action = 'Cerrar', duration = 5000) {
    try {
      this.logger.warn('User error shown:', message);
      this.snack.open(message, action, { duration });
    } catch (e) {
      // swallow
      console.warn('Error showing snack', e);
    }
  }

  showValidationError(message: string) {
    this.showError(message, 'Ok', 7000);
  }
}
