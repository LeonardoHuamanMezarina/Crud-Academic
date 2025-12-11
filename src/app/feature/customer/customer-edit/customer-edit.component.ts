import { Component, OnInit } from '@angular/core';
import { Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CustomerService } from '../../../core/services/customer.service';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';

// iconos de material
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

//librerias
import Swal from 'sweetalert2';
import { PendingChangesGuard } from '../../../core/guards/pending-changes.guard';

@Component({
  standalone: true,
  selector: 'app-customer-edit',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule
  ],
  templateUrl: './customer-edit.component.html',
  styleUrl: './customer-edit.component.scss'
})
export class CustomerEditComponent implements OnInit {
  form: FormGroup;
  maxDate: Date; // Fecha máxima permitida (18 años atrás)
  private initialFormValue: any = null;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<CustomerEditComponent>,
    private pendingGuard: PendingChangesGuard
  ) {
    // Calcular la fecha máxima (18 años atrás desde hoy)
    this.maxDate = new Date();
    this.maxDate.setFullYear(this.maxDate.getFullYear() - 18);

    this.form = this.fb.group({
      idUsuario: [null], // Solo para identificar el registro a actualizar
      firstName: ['', [Validators.required, Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)]],
      lastName: ['', [Validators.required, Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)]],
      email: ['', [Validators.required, Validators.email]], // Agregar email de vuelta
      documentType: ['', Validators.required],
      documentNumber: ['', Validators.required],
      birthDate: ['', [Validators.required, this.minimumAgeValidator(18)]],
      phone: ['', [Validators.required, Validators.pattern(/^9\d{8}$/)]],
      address: ['', Validators.required],
    });

    // Cambia la validación según el tipo de documento
    this.form.get('documentType')?.valueChanges.subscribe(tipo => {
      const numeroCtrl = this.form.get('documentNumber');
      if (tipo === 'DNI') {
        numeroCtrl?.setValidators([
          Validators.required,
          Validators.pattern(/^\d{8}$/)
        ]);
        numeroCtrl?.setValue('');
      } else if (tipo === 'CARNET') {
        numeroCtrl?.setValidators([
          Validators.required,
          Validators.pattern(/^\d{15}$/)
        ]);
        numeroCtrl?.setValue('');
      } else {
        numeroCtrl?.clearValidators();
        numeroCtrl?.setValue('');
      }
      numeroCtrl?.updateValueAndValidity();
    });
  }

  // Validador personalizado para edad mínima
  minimumAgeValidator(minAge: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null; // No validar si está vacío (required se encarga de eso)
      }
      
      const birthDate = new Date(control.value);
      const today = new Date();
      const age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      
      // Ajustar si no ha cumplido años este año
      const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()) 
        ? age - 1 
        : age;
      
      return actualAge >= minAge ? null : { minimumAge: { requiredAge: minAge, actualAge } };
    };
  }

  ngOnInit() {
    if (this.data && this.data.idUsuario) {
      // Copiar los datos y convertir la fecha correctamente
      const customerData = { ...this.data };
      
      // Si hay birthDate, convertirla a formato Date correcto
      if (customerData.birthDate) {
        console.log('Fecha original recibida:', customerData.birthDate);
        
        // Si viene como string, parsearlo correctamente
        if (typeof customerData.birthDate === 'string') {
          // Si viene en formato ISO (YYYY-MM-DD o YYYY-MM-DDTHH:mm:ss)
          if (customerData.birthDate.includes('-')) {
            // Extraer solo la parte de la fecha (YYYY-MM-DD)
            const dateOnly = customerData.birthDate.split('T')[0];
            customerData.birthDate = new Date(dateOnly + 'T00:00:00');
          } 
          // Si está en formato DD/MM/YYYY desde la tabla
          else if (customerData.birthDate.includes('/')) {
            const parts = customerData.birthDate.split('/');
            // Formato DD/MM/YYYY
            customerData.birthDate = new Date(parseInt(parts[2]), parseInt(parts[1]) - 1, parseInt(parts[0]));
          }
        }
        
        console.log('Fecha convertida para el formulario:', customerData.birthDate);
      }
      
      this.form.patchValue(customerData);
      // Guardar snapshot inicial para comparar cambios
      this.initialFormValue = this.form.getRawValue();
    }
  }

  actualizarCliente() {
    if (this.form.valid) {
      this.customerService.updateClient(this.form.value).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: '¡Éxito!',
            text: 'El cliente fue actualizado correctamente.',
            confirmButtonText: 'Aceptar'
          });
          this.dialogRef.close();
        },
        error: (err) => {
          console.error('Error al actualizar:', err);
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo actualizar el cliente.',
            confirmButtonText: 'Aceptar'
          });
        }
      });
    } else {
      this.form.markAllAsTouched();
    }
  }

    // Solo permite números enteros en el input
  soloNumeros(event: KeyboardEvent) {
    const charCode = event.key.charCodeAt(0);
    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
    }
  }


  // Solo permite números de 9 digitos
  telefono(event: KeyboardEvent) {
    if (!/[0-9]/.test(event.key) || this.form.get('phone')?.value?.length >= 9) {
      event.preventDefault();
    }
  }

    onSubmit() {
    if (this.form.valid) {
      console.log(this.form.value);
      this.form.reset();
    } else {
      this.form.markAllAsTouched(); // Marca todos los campos como tocados para mostrar los errores
    }
  }

    // Método para que el PendingChangesGuard consulte si hay cambios sin guardar
    hasUnsavedChanges(): boolean {
      if (!this.initialFormValue) return false;
      const current = this.form.getRawValue();
      return JSON.stringify(current) !== JSON.stringify(this.initialFormValue);
    }

    // Método usado por el diálogo para confirmar cierre si hay cambios sin guardar
    async closeWithConfirm() {
      // Delegate to PendingChangesGuard so route and dialog use the same logic
      try {
        const can = await this.pendingGuard.canDeactivate(this as any);
        if (can) this.dialogRef.close();
      } catch (e) {
        // On error, close to avoid blocking the user
        this.dialogRef.close();
      }
    }
  
}