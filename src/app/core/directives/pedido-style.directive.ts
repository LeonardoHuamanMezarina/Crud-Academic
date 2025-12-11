import { Directive, Input, ElementRef, Renderer2, OnChanges, SimpleChanges } from '@angular/core';

/**
 * Simple attribute directive to style a pedido row based on its total value.
 * Usage:
 * <tr [appPedidoStyle]="pedido.total"></tr>
 * Optional inputs: [threshold], [highColor], [lowColor]
 */
@Directive({
  selector: '[appPedidoStyle]',
  standalone: true
})
export class PedidoStyleDirective implements OnChanges {
  @Input('appPedidoStyle') total: number | string | null = 0;
  @Input() threshold = 50; // default threshold in the same currency unit
  // class names to toggle
  @Input() highClass = 'pedido-high';
  @Input() lowClass = 'pedido-low';

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(changes: SimpleChanges) {
    this.update();
  }

  private parseTotal(): number {
    const v = this.total ?? 0;
    if (typeof v === 'number') return v;
    // remove currency symbols and thousands separators
    const cleaned = String(v).replace(/[^0-9.-]+/g, '');
    const n = parseFloat(cleaned);
    return isFinite(n) ? n : 0;
  }

  private update() {
    const value = this.parseTotal();
    const host = this.el.nativeElement;
    if (value > this.threshold) {
      // add high class, remove low class
      this.renderer.addClass(host, this.highClass);
      this.renderer.removeClass(host, this.lowClass);
    } else {
      // ensure high class removed; optionally add low class
      this.renderer.removeClass(host, this.highClass);
      this.renderer.addClass(host, this.lowClass);
    }
  }
}
