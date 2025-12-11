import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PedidoListComponent } from './pedido-list/pedido-list.component';
import { PedidoDetailComponent } from './pedido-detail/pedido-detail.component';
import { AuthGuard } from '../../core/guards/auth.guard';

const routes: Routes = [
  { path: '', component: PedidoListComponent, canActivate: [AuthGuard] },
  { path: ':id', component: PedidoDetailComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [CommonModule, RouterModule.forChild(routes), PedidoListComponent, PedidoDetailComponent],
})
export class PedidoModule {}
