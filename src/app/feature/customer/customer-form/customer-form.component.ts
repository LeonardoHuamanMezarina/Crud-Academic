import { Component, OnInit } from '@angular/core';
import { Inject, Optional } from '@angular/core';
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
  selector: 'app-customer-form',
  standalone: true,
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
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent implements OnInit{

  form: FormGroup;
  maxDate: Date; // Fecha máxima permitida (18 años atrás)

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    @Inject(MAT_DIALOG_DATA) @Optional() public data: any = null, // <-- Opcional para usar en rutas
    @Optional() private dialogRef: MatDialogRef<CustomerFormComponent>, // <-- Opcional para usar en rutas
    private pendingGuard: PendingChangesGuard

  ) {
    // Calcular la fecha máxima (18 años atrás desde hoy)
    this.maxDate = new Date();
    this.maxDate.setFullYear(this.maxDate.getFullYear() - 18);

    this.form = this.fb.group({
      idUsuario: [null],
      username: [''],
      contra: [''],
      firstName: ['', [
        Validators.required, 
        Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/),
        this.validName.bind(this),
        this.noExcessiveSpaces.bind(this)
      ]],
      lastName: ['', [
        Validators.required, 
        Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/),
        this.validName.bind(this),
        this.noExcessiveSpaces.bind(this)
      ]],
      email: ['', [Validators.required, Validators.email]],
      documentType: ['', Validators.required],
      documentNumber: ['', Validators.required],
      birthDate: ['', [Validators.required, this.minimumAgeValidator(18)]],
      phone: ['', [
        Validators.required, 
        Validators.pattern(/^9\d{8}$/),
        this.noRepeatedNumbers.bind(this)
      ]],
      address: ['', [
        Validators.required,
        this.noExcessiveSpaces.bind(this)
      ]]
    });

    // Cambia la validación según el tipo de documento
    this.form.get('documentType')?.valueChanges.subscribe(tipo => {
      const numeroCtrl = this.form.get('documentNumber');
      if (tipo === 'DNI') {
        numeroCtrl?.setValidators([
          Validators.required,
          Validators.pattern(/^\d{8}$/),
          this.noRepeatedNumbers.bind(this)
        ]);
        numeroCtrl?.setValue('');
      } else if (tipo === 'CARNET') {
        numeroCtrl?.setValidators([
          Validators.required,
          Validators.pattern(/^\d{15}$/),
          this.noRepeatedNumbers.bind(this)
        ]);
        numeroCtrl?.setValue('');
      } else {
        numeroCtrl?.clearValidators();
        numeroCtrl?.setValue('');
      }
      numeroCtrl?.updateValueAndValidity();
    });
  }

  // Validador para espacios excesivos
  noExcessiveSpaces(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    
    const value = control.value.toString();
    // Verificar si hay más de un espacio consecutivo
    if (/\s{2,}/.test(value)) {
      return { excessiveSpaces: true };
    }
    
    // Verificar si empieza o termina con espacios
    if (value.trim() !== value) {
      return { leadingTrailingSpaces: true };
    }
    
    return null;
  }

  // Validador para números repetitivos
  noRepeatedNumbers(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    
    const value = control.value.toString();
    
    // Verificar si todos los dígitos son iguales
    if (/^(.)\1+$/.test(value)) {
      return { repeatedNumbers: true };
    }
    
    return null;
  }

  // Validador para nombres (solo letras y un espacio entre palabras)
  validName(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    
    const value = control.value.toString().trim();
    
    // Solo letras y espacios simples, no puede empezar ni terminar con espacio
    if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+(\s[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$/.test(value)) {
      return { invalidName: true };
    }
    
    return null;
  }

  // Método para limpiar espacios al escribir
  cleanSpaces(event: any) {
    const input = event.target;
    let value = input.value;
    
    // Reemplazar múltiples espacios por uno solo
    value = value.replace(/\s+/g, ' ');
    
    // Evitar espacios al inicio
    if (value.startsWith(' ')) {
      value = value.trimStart();
    }
    
    input.value = value;
    
    // Actualizar el control del formulario
    const controlName = input.getAttribute('formControlName');
    if (controlName) {
      this.form.get(controlName)?.setValue(value);
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

  guardarCliente() {
    if (this.form.valid) {
      this.customerService.saveClient(this.form.value).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: '¡Éxito!',
            text: 'El cliente fue guardado correctamente.',
            confirmButtonText: 'Aceptar'
          });
          this.form.reset();
          // Solo cerrar el dialog si existe (cuando se usa como modal)
          if (this.dialogRef) {
            this.dialogRef.close();
          }
          
        },
        error: (err) => {
          console.error('Error al guardar:', err); // Muestra el error exacto en la consola
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo guardar el cliente.',
            confirmButtonText: 'Aceptar'
          });
        }
      });
    } else {
      this.form.markAllAsTouched();
    }
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
      this.form.patchValue(this.data); // Rellena el formulario con los datos recibidos
    }
  }

  // Confirm before closing the dialog if the form has unsaved changes
  async closeWithConfirm() {
    try {
      const can = await this.pendingGuard.canDeactivate(this as any);
      if (can && this.dialogRef) this.dialogRef.close();
    } catch (e) {
      if (this.dialogRef) this.dialogRef.close();
    }
  }

  // Provide hasUnsavedChanges for PendingChangesGuard when using this component as dialog
  hasUnsavedChanges(): boolean {
    return this.form.dirty || this.form.touched;
  }
}