import { ApplicationConfig, provideZoneChangeDetection, LOCALE_ID, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

//new import
import { provideHttpClient, withFetch, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';
import { GlobalErrorHandler } from './core/services/global-error-handler';
import { ErrorHandler } from '@angular/core';
import { provideNativeDateAdapter, MAT_DATE_LOCALE } from '@angular/material/core';

// Registrar el locale español
registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(routes), 
  provideHttpClient(withFetch()),
  provideNativeDateAdapter(),
  importProvidersFrom(FormsModule, MatSnackBarModule),
    { provide: MAT_DATE_LOCALE, useValue: 'es' },
    { provide: LOCALE_ID, useValue: 'es' }
    ,{ provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
    ,{ provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
    ,{ provide: ErrorHandler, useClass: GlobalErrorHandler }
  ]
};

