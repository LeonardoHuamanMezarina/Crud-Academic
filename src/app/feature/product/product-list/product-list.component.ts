// src/app/feature/product/product-list/product-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/interfaces/product';
import { HttpErrorResponse } from '@angular/common/http';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { HeaderComponent } from '../../../layout/header/header.component';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog'; 
import { ProductFormComponent } from '../product-form/product-form.component'; 
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { CoreDirectivesModule } from '../../../core/core-directives.module';
import Swal from 'sweetalert2'; 

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
      CoreDirectivesModule,
    SidebarComponent,
    HeaderComponent,
    MatIconModule,
    MatTableModule,
    MatButtonModule
  ]
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';

  // Propiedades para mat-table
  displayedColumns: string[] = ['name', 'specification', 'description', 'category', 'unitPrice', 'stock', 'status', 'internalCode', 'registrationDate', 'brandId', 'acciones'];
  dataSource = this.products;

  constructor(
    private router: Router,
    private productService: ProductService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  /**
   * Loads the list of products from the Back-End.
   */
  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    console.log('Iniciando carga de productos...');

    this.productService.getProducts().subscribe({
      next: (data: Product[]) => {
        this.products = data;
        this.dataSource = this.products;
        this.isLoading = false;
        console.log('Productos cargados exitosamente:', this.products);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error loading products:', err);
        this.errorMessage = `No se pudieron cargar los productos. Código: ${err.status || 'desconocido'}. Por favor, intente de nuevo más tarde.`;
        this.isLoading = false;
      }
    });
  }

  /**
   * Opens the dialog to create a new product.
   */
  goToFormulario(): void {
    const dialogRef = this.dialog.open(ProductFormComponent, {
      width: '600px',
      disableClose: false,
      data: null // No hay datos para un nuevo producto
    });

    dialogRef.afterClosed().subscribe(result => {
      // Recargar la lista de productos después de cerrar el dialog
      this.loadProducts();
    });
  }

  /**
   * Navigates to the form to edit an existing product.
   * @param id The ID of the product to edit.
   */
  editProduct(id: number | undefined): void {
    console.log('Botón Editar clickeado para ID:', id);
    if (id) {
      const dialogRef = this.dialog.open(ProductFormComponent, {
        width: '600px',
        disableClose: false,
        data: { productId: id } // Pasar el ID del producto a editar
      });

      dialogRef.afterClosed().subscribe(result => {
        // Recargar la lista de productos después de cerrar el dialog
        this.loadProducts();
      });
    } else {
      console.warn('No se pudo editar el producto: ID no definido o nulo.');
      this.errorMessage = 'No se pudo editar el producto porque su ID no es válido.';
    }
  }

  /**
   * Muestra el modal de confirmación SweetAlert2 antes de eliminar un producto.
   * @param id El ID del producto a eliminar.
   * @param name El nombre del producto a eliminar.
   */
  confirmDeleteProduct(id: number | undefined, name: string): void {
    if (id) {
      Swal.fire({
        title: '¿Estás seguro?',
        text: `¿Realmente quieres eliminar el producto: ${name}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
      }).then((result) => {
        if (result.isConfirmed) {
          this.executeDelete(id, name);
        }
      });
    } else {
      this.errorMessage = 'No se pudo eliminar el producto: ID no válido.';
    }
  }

  /**
   * Ejecuta la eliminación lógica del producto y muestra resultado con SweetAlert2.
   */
  executeDelete(id: number, name: string): void {
    this.productService.deleteProduct(id).subscribe({
      next: () => {
        console.log(`Producto con ID ${id} eliminado lógicamente.`);
        Swal.fire({
          title: '¡Producto Eliminado!',
          text: `El producto '${name}' ha sido eliminado correctamente.`,
          icon: 'success',
          confirmButtonText: 'Entendido'
        }).then(() => {
          this.loadProducts(); // Recarga la lista para mostrar el cambio de estado
        });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error eliminando producto:', err);
        this.errorMessage = `Hubo un error al intentar eliminar el producto. Código: ${err.status || 'desconocido'}.`;
        Swal.fire({
          title: 'Error',
          text: this.errorMessage,
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }

  /**
   * Muestra el modal de confirmación SweetAlert2 antes de restaurar un producto.
   * @param id El ID del producto a restaurar.
   * @param name El nombre del producto a restaurar.
   */
  confirmRestoreProduct(id: number | undefined, name: string): void {
    if (id) {
      Swal.fire({
        title: '¿Restaurar Producto?',
        text: `¿Estás seguro de que quieres restaurar el producto: ${name}?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#28a745',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, restaurar',
        cancelButtonText: 'Cancelar'
      }).then((result) => {
        if (result.isConfirmed) {
          this.executeRestore(id, name);
        }
      });
    } else {
      this.errorMessage = 'No se pudo restaurar el producto: ID no válido.';
    }
  }

  /**
   * Ejecuta la restauración del producto y muestra resultado con SweetAlert2.
   */
  executeRestore(id: number, name: string): void {
    this.productService.restoreProduct(id).subscribe({
      next: () => {
        console.log(`Producto con ID ${id} restaurado.`);
        Swal.fire({
          title: '¡Producto Restaurado!',
          text: `El producto '${name}' ha sido restaurado correctamente.`,
          icon: 'success',
          confirmButtonText: 'Entendido'
        }).then(() => {
          this.loadProducts(); // Recarga la lista para mostrar el cambio de estado
        });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error restaurando producto:', err);
        this.errorMessage = `Hubo un error al intentar restaurar el producto. Código: ${err.status || 'desconocido'}.`;
        Swal.fire({
          title: 'Error',
          text: this.errorMessage,
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }

  /**
   * Helper to format the date if it comes as a string.
   * @param dateString The date string from the API.
   * @returns Formatted date string.
   */
  formatDate(dateString: string): string {
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };
    try {
        return new Date(dateString).toLocaleDateString('es-ES', options);
    } catch (e) {
        console.error('Error formatting date:', e, 'Input:', dateString);
        return dateString;
    }
  }

  report(){
      this.productService.reportPdf().subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'reporte.pdf'; // nombre temporal
      link.click();
      URL.revokeObjectURL(url);
    });

    
  }


}
