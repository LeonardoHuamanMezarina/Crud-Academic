import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
// MatSelect removed from dialog; sorting moved to global listing
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/interfaces/product';
import { firstValueFrom } from 'rxjs';
import { Pedido, SaleDetail } from '../../../core/interfaces/pedido';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  standalone: true,
  selector: 'app-pedido-detail',
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatTableModule, MatIconModule],
  templateUrl: './pedido-detail.component.html',
  styleUrls: ['./pedido-detail.component.scss']
})
export class PedidoDetailComponent implements OnInit {
  pedido: Pedido;
  dataSource = new MatTableDataSource<SaleDetail>([]);
  displayedColumns: string[] = ['productId', 'unitPrice', 'quantity', 'subtotal'];
  loading = false;
  error: any = null;
  // map idProduct -> Product (to access name and unitPrice)
  productMap = new Map<number, Product>();

  constructor(@Inject(MAT_DIALOG_DATA) public data: Pedido, private dialogRef: MatDialogRef<PedidoDetailComponent>, private productService: ProductService) {
    this.pedido = data;
    this.setDetails(this.pedido?.details || []);
  }

  async ngOnInit(): Promise<void> {
    // Cargar nombres de productos para mostrar en la tabla (si aplica)
    await this.loadProductNames();
  }

  private async loadProductNames() {
    // Mejor: obtener la lista completa de productos y mapear idProduct -> name
    this.loading = true;
    try {
      const products = await firstValueFrom(this.productService.getProducts());
      console.debug('[PedidoDetail] fetched products count:', (products || []).length);
      (products || []).forEach(p => {
        if (p.idProduct != null) this.productMap.set(p.idProduct, p);
      });
      console.debug('[PedidoDetail] productMap keys:', Array.from(this.productMap.keys()).slice(0,50));
    } catch (err) {
      this.error = err;
      console.error('[PedidoDetail] error loading products', err);
    } finally {
      this.loading = false;
    }
  }

  volver() {
    this.dialogRef.close();
  }

  setDetails(details: SaleDetail[]) {
    const arr = [...(details || [])];
    // en el diálogo mostramos por defecto recientes primero
    this.dataSource.data = arr.sort((a,b) => (new Date(b.registrationDate || '').getTime() || 0) - (new Date(a.registrationDate || '').getTime() || 0));
    console.debug('[PedidoDetail] setDetails count:', this.dataSource.data.length);
    this.dataSource.data.forEach(d => {
      const prod = this.productMap.get(d.productId as number);
      console.debug('[PedidoDetail] detail', d.productId, '->', prod?.name, 'unitPrice=', prod?.unitPrice, 'detail.unitPrice=', d.unitPrice);
    });
  }
}

