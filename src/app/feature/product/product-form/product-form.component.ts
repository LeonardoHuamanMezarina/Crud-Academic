// src/app/feature/product/product-form/product-form.component.ts
import { Component, OnInit } from '@angular/core';
import { Inject, Optional } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  AbstractControl
} from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/interfaces/product';
import { HttpErrorResponse } from '@angular/common/http';

// Angular Material imports
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

//librerias
import Swal from 'sweetalert2';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html', // Asegúrate que este nombre de archivo sea correcto
  styleUrls: ['./product-form.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatButtonModule
  ]
})
export class ProductFormComponent implements OnInit {
  productForm: FormGroup;
  productId: number | null = null;
  isEditMode: boolean = false;
  isLoading: boolean = false;
  errorMessage: string = '';
  showOutOfStockWarning: boolean = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private productService: ProductService,
    @Inject(MAT_DIALOG_DATA) @Optional() public data: any = null,
    @Optional() private dialogRef: MatDialogRef<ProductFormComponent>
  ) {
    this.productForm = this.fb.group({
      idProduct: [null],
      name: ['', [Validators.required, Validators.maxLength(55)]],
      specification: ['', Validators.required],
      description: ['', Validators.required],
      category: ['', [Validators.required, Validators.maxLength(55), Validators.pattern(/^[a-zA-Z\s]*$/)]],
      unitPrice: [null, [Validators.required, Validators.min(0.01)]],
      stock: [null, [Validators.required, Validators.min(0), Validators.max(100)]],
      status: ['A', Validators.required],
      internalCode: ['', [Validators.required]],
      registrationDate: [null],
      brandId: [null, [Validators.required, Validators.min(1)]]
    });

    this.productForm.get('stock')?.valueChanges.subscribe(value => {
      this.showOutOfStockWarning = (value === 0 && this.productForm.get('stock')?.touched) ? true : false;
    });
  }

  ngOnInit(): void {
    // Si hay datos del dialog (modal), usar esos datos
    if (this.data && this.data.productId) {
      this.productId = this.data.productId;
      this.isEditMode = true;
      if (this.productId) {
        this.loadProduct(this.productId);
      }
    } else {
      // Si no hay datos del dialog, verificar parámetros de ruta (acceso directo)
      this.activatedRoute.paramMap.subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.productId = +id;
          this.isEditMode = true;
          this.loadProduct(this.productId);
        } else {
          this.isEditMode = false;
          this.productForm.reset({ status: 'A' });
        }
      });
    }
  }

  loadProduct(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.productService.getProductById(id).subscribe({
      next: (product: Product) => {
        this.productForm.patchValue({
          idProduct: product.idProduct,
          name: product.name,
          specification: product.specification,
          description: product.description,
          category: product.category,
          unitPrice: product.unitPrice,
          stock: product.stock,
          status: product.status,
          internalCode: product.internalCode,
          brandId: product.brandId
        });
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error al cargar producto para edición:', err);
        this.errorMessage = `No se pudo cargar el producto para edición. Código: ${err.status || 'desconocido'}.`;
        this.isLoading = false;
      }
    });
  }

  saveProduct(): void {
    this.errorMessage = '';

    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      this.errorMessage = 'Por favor, completa todos los campos requeridos correctamente.';
      return;
    }

    this.isLoading = true;
    const productData: Product = this.productForm.value;

    if (this.isEditMode && this.productId) {
      this.productService.updateProduct(productData).subscribe({
        next: (responseProduct: Product) => {
          this.isLoading = false;
          Swal.fire({
            icon: 'success',
            title: '¡Producto Actualizado!',
            html: `
              <div style="text-align: left; margin: 20px;">
                <strong>Nombre:</strong> ${responseProduct.name}<br>
                <strong>Especificación:</strong> ${responseProduct.specification}<br>
                <strong>Descripción:</strong> ${responseProduct.description}<br>
                <strong>ID Marca:</strong> ${responseProduct.brandId}<br>
                <strong>Categoría:</strong> ${responseProduct.category}<br>
                <strong>Precio Unitario:</strong> S/. ${responseProduct.unitPrice}<br>
                <strong>Stock:</strong> ${responseProduct.stock} unidades<br>
                <strong>Código Interno:</strong> ${responseProduct.internalCode}
              </div>
            `,
            confirmButtonText: 'Aceptar'
          }).then(() => {
            // Si está en un dialog, cerrarlo; si no, navegar
            if (this.dialogRef) {
              this.dialogRef.close();
            } else {
              this.goToListado();
            }
          });
          console.log('Producto actualizado:', responseProduct);
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          console.error('Error al actualizar producto:', err);
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: `No se pudo actualizar el producto: ${err.error?.message || err.message || 'Error desconocido'}`,
            confirmButtonText: 'Aceptar'
          });
        }
      });
    } else {
      const productToCreate: Omit<Product, 'idProduct' | 'registrationDate' | 'status'> = {
          name: productData.name,
          specification: productData.specification,
          description: productData.description,
          category: productData.category,
          unitPrice: productData.unitPrice,
          stock: productData.stock,
          internalCode: productData.internalCode,
          brandId: productData.brandId
      };

      this.productService.createProduct(productToCreate).subscribe({
        next: (responseProduct: Product) => {
          this.isLoading = false;
          Swal.fire({
            icon: 'success',
            title: '¡Producto Creado!',
            html: `
              <div style="text-align: left; margin: 20px;">
                <strong>Nombre:</strong> ${responseProduct.name}<br>
                <strong>Especificación:</strong> ${responseProduct.specification}<br>
                <strong>Descripción:</strong> ${responseProduct.description}<br>
                <strong>ID Marca:</strong> ${responseProduct.brandId}<br>
                <strong>Categoría:</strong> ${responseProduct.category}<br>
                <strong>Precio Unitario:</strong> S/. ${responseProduct.unitPrice}<br>
                <strong>Stock:</strong> ${responseProduct.stock} unidades<br>
                <strong>Código Interno:</strong> ${responseProduct.internalCode}
              </div>
            `,
            confirmButtonText: 'Aceptar'
          }).then(() => {
            this.productForm.reset({ status: 'A' });
            // Si está en un dialog, cerrarlo; si no, navegar
            if (this.dialogRef) {
              this.dialogRef.close();
            } else {
              this.goToListado();
            }
          });
          console.log('Producto creado:', responseProduct);
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          console.error('Error al crear producto:', err);
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: `No se pudo crear el producto: ${err.error?.message || err.message || 'Error desconocido'}`,
            confirmButtonText: 'Aceptar'
          });
        }
      });
    }
  }

  goToListado(): void {
    this.router.navigate(['/product-list']); // Navega al listado (asegúrate de que esta ruta sea correcta)
  }
}
