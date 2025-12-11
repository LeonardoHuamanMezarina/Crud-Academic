import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PedidoService } from '../../../core/services/pedido.service';
import { Pedido } from '../../../core/interfaces/pedido';
import { Router } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { HeaderComponent } from '../../../layout/header/header.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CoreDirectivesModule } from '../../../core/core-directives.module';
import { PedidoDetailComponent } from '../pedido-detail/pedido-detail.component';
import { PedidoFormComponent } from '../pedido-form/pedido-form.component';

@Component({
  standalone: true,
  selector: 'app-pedido-list',
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, SidebarComponent, HeaderComponent, MatDialogModule, CoreDirectivesModule],
  templateUrl: './pedido-list.component.html',
  styleUrls: ['./pedido-list.component.scss']
})
export class PedidoListComponent implements OnInit {
  private pedidoService = inject(PedidoService);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  dataSource = new MatTableDataSource<Pedido>();
  displayedColumns: string[] = ['idPedido', 'cliente', 'total', 'cantidad', 'fechaPedido', 'status', 'acciones'];
  // raw list returned from backend
  allPedidos: Pedido[] = [];
  // filters
  sortRecent = true; // true = recientes desc, false = antiguos asc
  statusFilter: string = 'ALL';

  ngOnInit(): void {
    this.findAll();
  }

  findAll() {
    this.pedidoService.findAll().subscribe({
      next: (res) => {
        this.allPedidos = res || [];
        this.applyFilters();
      },
      error: (err) => {
        console.error('Error cargando pedidos', err);
      }
    });
  }

  // apply status filter and sort order to allPedidos and update dataSource
  applyFilters() {
    let items = [...(this.allPedidos || [])];
    // filter by status if not ALL
    if (this.statusFilter && this.statusFilter !== 'ALL') {
      const sf = this.statusFilter;
      items = items.filter(p => (p.status || '').toString().toUpperCase().startsWith(sf));
    }
    // sort by registrationDate
    items.sort((a,b) => {
      const ta = new Date(a.registrationDate || a.fechaPedido || '').getTime() || 0;
      const tb = new Date(b.registrationDate || b.fechaPedido || '').getTime() || 0;
      return this.sortRecent ? (tb - ta) : (ta - tb);
    });
    this.dataSource.data = items;
  }

  onSortChange(value: string) {
    this.sortRecent = value === 'true' || value === 'Recientes' || value === 'recent';
    this.applyFilters();
  }

  onStatusChange(value: string) {
    this.statusFilter = value || 'ALL';
    this.applyFilters();
  }

  openAddDialog() {
    // abrir formulario de creación de venta; pasar userId por defecto 163
    const ref = this.dialog.open(PedidoFormComponent, { width: '800px', data: { userId: 163 } });
    ref.afterClosed().subscribe(res => { if (res) this.findAll(); });
  }

  verPedido(id?: number) {
    if (!id) return;
    // Obtener pedido con detalles y abrir diálogo
    this.pedidoService.getById(id).subscribe({
      next: p => {
        this.dialog.open(PedidoDetailComponent, { width: '900px', data: p });
      },
      error: e => console.error('Error cargando pedido', e)
    });
  }
}
