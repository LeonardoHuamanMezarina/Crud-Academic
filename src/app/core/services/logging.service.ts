import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoggingService {
  log(message: string, data?: any) {
    console.log(`[LOG] ${message}`, data ?? '');
  }
  error(message: string, data?: any) {
    console.error(`[ERROR] ${message}`, data ?? '');
  }
  warn(message: string, data?: any) {
    console.warn(`[WARN] ${message}`, data ?? '');
  }
}
