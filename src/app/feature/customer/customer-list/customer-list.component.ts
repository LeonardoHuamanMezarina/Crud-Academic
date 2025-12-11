import { Component, inject, OnInit } from '@angular/core';
import { CustomerFormComponent } from '../customer-form/customer-form.component';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { CommonModule } from '@angular/common';
import { TruncatePipe } from '../../../core/pipes/truncate.pipe';
import { ActivatedRoute } from '@angular/router';
import { CustomerEditComponent } from '../customer-edit/customer-edit.component';
import { HeaderComponent } from '../../../layout/header/header.component';

//librerias
import { MatDialog } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableDataSource } from '@angular/material/table';

import Swal from 'sweetalert2';


//apis
import { Customer } from '../../../core/interfaces/customer';
import { CustomerService } from '../../../core/services/customer.service';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-customer-list',
  imports: [
    CommonModule,
    MatButtonModule,
    SidebarComponent,
    MatIconModule,
    MatTableModule,
    HeaderComponent
    ,TruncatePipe
  ],
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss'
})

export class CustomerListComponent implements OnInit {

  //Variables para inject
  private router = inject(Router);
  private customerService = inject(CustomerService);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);

  dataSource = new MatTableDataSource<Customer>();
  // sortDesc = true => newest first (desc by id)
  sortDesc = true;
  // raw list kept for filtering/sorting
  allCustomers: Customer[] = [];
  // statusFilter: 'ALL' | 'A' (activo) | 'I' (inactivo)
  statusFilter: 'ALL' | 'A' | 'I' = 'ALL';

  displayedColumns: string[] = 
  [
    'idUsuario', 
    'firstName', 
    'lastName', 
    'email', 
    'documentType', 
    'documentNumber',
    'birthDate', 
    'phone', 
    'status',
    'address', 
    'registrationDate', 
    'acciones'
  ];


  ngOnInit(): void {
    // Si el resolver proveyó datos, úsalos; si no, haz la petición normal
    const resolved = this.route.snapshot.data['customers'];
    if (resolved && Array.isArray(resolved)) {
      this.allCustomers = [...resolved];
      this.applyFilters();
    } else {
      this.findAll();
    }
}


  findAll(): void {
    this.customerService.findAll().subscribe((response: Customer[]) => {
      this.allCustomers = response || [];
      this.applyFilters();
    });
}

  sortData(arr: Customer[]) {
    const copy = [...arr];
    if (this.sortDesc) {
      // newest first (higher id first)
      return copy.sort((a: any, b: any) => (+b.idUsuario || 0) - (+a.idUsuario || 0));
    }
    // oldest first
    return copy.sort((a: any, b: any) => (+a.idUsuario || 0) - (+b.idUsuario || 0));
  }

  toggleSort() {
    this.sortDesc = !this.sortDesc;
    // re-sort current data
    this.applyFilters();
  }

  // Handler para el select de orden
  onSortSelect(event: Event) {
    const target = event.target as HTMLSelectElement;
    // los valores vienen como strings "true"/"false"
    this.sortDesc = target.value === 'true';
    this.applyFilters();
  }

  setStatusFilter(value: 'ALL' | 'A' | 'I') {
    this.statusFilter = value;
    this.applyFilters();
  }

  // Handler para el select de estado
  onStatusSelect(event: Event) {
    const target = event.target as HTMLSelectElement;
    const value = target.value as 'ALL' | 'A' | 'I';
    this.setStatusFilter(value);
  }

  applyFilters() {
    let filtered = [...(this.allCustomers || [])];
    if (this.statusFilter === 'A') {
      filtered = filtered.filter(c => c.status === 'A');
    } else if (this.statusFilter === 'I') {
      filtered = filtered.filter(c => c.status !== 'A');
    }
    this.dataSource.data = this.sortData(filtered);
  }


  //funcion para abrir el dialogo de formulario /feature/customer/Customer-form
  openCreateDialog() {
    const dialogRef = this.dialog.open(CustomerFormComponent, {
      width: '600px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(() => {
      this.findAll(); // Refresca la tabla después de cerrar el formulario
    });
}


  eliminarCliente(idUsuario: number) {
  Swal.fire({
    title: '¿Estás seguro?',
    text: 'Esta acción no se puede deshacer.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar'
  }).then((result) => {
    if (result.isConfirmed) {
      this.customerService.delete(idUsuario).subscribe({
        next: () => {
          Swal.fire('Eliminado', 'El cliente ha sido eliminado.', 'success');
          this.findAll(); // Actualiza la tabla
        },
        error: (err) => {
          console.error('Error al eliminar:', err);
          Swal.fire('Error', 'No se pudo eliminar el cliente.', 'error');
        }
      });
    }
  });
}


  editarCliente(customer: Customer) {
  console.log('Datos del cliente enviados al formulario de edición:', customer);
  console.log('Fecha de nacimiento original:', customer.birthDate);
  
  const dialogRef = this.dialog.open(CustomerEditComponent, {
    width: '600px',
    data: customer, // Pasas el objeto completo al nuevo componente
    disableClose: true // deshabilita cierre por backdrop/ESC para controlar confirmación
  });

  dialogRef.afterClosed().subscribe(() => {
    this.findAll(); // Refresca la tabla después de editar
  });
}

restaurarCliente(idUsuario: number) {
  this.customerService.restaurar(idUsuario).subscribe({
    next: () => {
      Swal.fire('Restaurado', 'El cliente ha sido restaurado.', 'success');
      this.findAll(); // Refresca la tabla
    },
    error: (err) => {
      console.error('Error al restaurar:', err);
      Swal.fire('Error', 'No se pudo restaurar el cliente.', 'error');
    }
  });
}

reportPdf() {
    this.customerService.reportPdf().subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'reporte.pdf'; // nombre temporal
      link.click();
      URL.revokeObjectURL(url);
    });
  }


}
