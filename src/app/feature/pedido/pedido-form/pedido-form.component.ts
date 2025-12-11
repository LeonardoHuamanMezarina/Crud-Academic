import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProductService } from '../../../core/services/product.service';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer } from '../../../core/interfaces/customer';
import { PedidoService } from '../../../core/services/pedido.service';
import { ErrorControllerService } from '../../../core/errors/error-controller.service';
import { Product } from '../../../core/interfaces/product';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-pedido-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatSelectModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './pedido-form.component.html',
  styleUrls: ['./pedido-form.component.scss']
})
export class PedidoFormComponent implements OnInit {
  form: FormGroup;
  products: Product[] = [];
  customers: Customer[] = [];
  loading = false;

  // userId puede recibirse en data o por defecto 163
  userId: number;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private customerService: CustomerService,
    private pedidoService: PedidoService,
    private errorCtrl: ErrorControllerService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<PedidoFormComponent>
  ) {
    this.userId = data?.userId ?? 163;

    this.form = this.fb.group({
      customerId: [this.userId, Validators.required],
      paymentType: ['EFECTIVO', Validators.required],
      receiptType: ['BOLETA', Validators.required],
      product: this.fb.array([])
    });
  }

  getControl(item: AbstractControl | null, name: string): FormControl {
    // helper para obtener FormControl con tipado correcto en la plantilla
    const group = item as FormGroup | null;
    return (group?.get(name) as FormControl) ?? new FormControl(null);
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCustomers();
    // empezar con una fila por defecto
    this.addRow();
  }

  private loadCustomers() {
    // cargar clientes para poder seleccionar el cliente registrado
    this.customerService.findAll().subscribe({
      next: cs => {
        this.customers = (cs || []).filter(c => c.status === 'A' || c.status === 'ACTIVE' || c.status === 'Activo');
      },
      error: err => {
        console.error('Error cargando clientes', err);
      }
    });
  }

  get items(): FormArray { return this.form.get('product') as FormArray; }

  addRow() {
    const row = this.fb.group({
      productId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.pattern(/^[1-9]\d*$/)]]
    });
    this.items.push(row);
  }

  removeRow(index: number) {
    this.items.removeAt(index);
  }

  private loadProducts() {
    this.loading = true;
    this.productService.getProducts().subscribe({
      next: prods => {
        // filtrar solo activos (status 'A')
        this.products = (prods || []).filter(p => p.status === 'A');
        this.loading = false;
      },
      error: err => {
        console.error('Error cargando productos', err);
        this.loading = false;
      }
    });
  }

  // limpia ceros a la izquierda y asegura entero positivo
  normalizeQuantity(controlName: string, idx: number) {
    const ctrl = (this.items.at(idx) as FormGroup).get(controlName);
    if (!ctrl) return;
    let val = String(ctrl.value || '');
    // eliminar ceros a la izquierda
    val = val.replace(/^0+/, '') || '0';
    // si queda 0, dejar 1 como mínimo
    if (val === '0') val = '1';
    ctrl.setValue(Number(val));
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // Validación extra: al menos un producto válido
    const items = this.form.value.product || [];
    const validItems = (items as any[]).filter(i => i && i.productId && Number(i.quantity) > 0);
    if (!validItems || validItems.length === 0) {
      this.errorCtrl.showValidationError('Debe agregar al menos un producto para registrar la venta.');
      return;
    }
    const selectedUserId = Number(this.form.value.customerId) || this.userId;
    const body = {
      paymentType: this.form.value.paymentType,
      receiptType: this.form.value.receiptType,
      product: this.form.value.product.map((p: any) => ({ productId: Number(p.productId), quantity: Number(p.quantity) }))
    };

    this.pedidoService.saveForUser(selectedUserId, body).subscribe({
      next: res => {
        Swal.fire({ icon: 'success', title: 'Venta registrada', text: 'La transacción fue guardada.' });
        this.dialogRef.close(true);
      },
      error: err => {
        console.error('Error guardando venta', err);
        this.errorCtrl.showError('No se pudo guardar la venta. Intente nuevamente.');
      }
    });
  }
}
