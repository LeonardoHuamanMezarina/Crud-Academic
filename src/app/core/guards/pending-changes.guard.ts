import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { Observable, firstValueFrom } from 'rxjs';
import Swal from 'sweetalert2';

export interface CanComponentDeactivate {
  hasUnsavedChanges: () => boolean | Observable<boolean> | Promise<boolean>;
}

@Injectable({ providedIn: 'root' })
export class PendingChangesGuard implements CanDeactivate<CanComponentDeactivate> {
  async canDeactivate(component: CanComponentDeactivate): Promise<boolean> {
    try {
      const dirty = component.hasUnsavedChanges();

      // Si devuelve Observable o Promise, resolverlo (usar firstValueFrom para Observables)
      const resolved = dirty instanceof Promise
        ? await dirty
        : (dirty instanceof Observable ? await firstValueFrom(dirty as Observable<boolean>) : dirty);

      if (typeof resolved === 'boolean') {
        if (resolved) {
          const result = await Swal.fire({
            title: 'Hay cambios sin guardar',
            text: 'Tienes cambios sin guardar. ¿Deseas salir y perder los cambios?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Salir',
            cancelButtonText: 'Cancelar',
          });
          return !!result.isConfirmed;
        }
        return true;
      }

      // Si no es boolean, permitir salida por defecto
      return true;
    } catch (e) {
      // Si el componente no define hasUnsavedChanges o hay error, permitir la salida
      return true;
    }
  }
}
