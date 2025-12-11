import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidoStyleDirective } from './directives/pedido-style.directive';
import { PedidoHoverDirective } from './directives/pedido-hover.directive';
import { LowStockBadgeDirective } from './directives/low-stock-badge.directive';

@NgModule({
  imports: [CommonModule, PedidoStyleDirective, PedidoHoverDirective, LowStockBadgeDirective],
  exports: [PedidoStyleDirective, PedidoHoverDirective, LowStockBadgeDirective]
})
export class CoreDirectivesModule {}
