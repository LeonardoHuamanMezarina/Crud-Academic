import { Directive, ElementRef, Renderer2, HostListener } from '@angular/core';

/**
 * Adds a subtle hover effect to table rows.
 * Usage: <tr appPedidoHover>...</tr>
 */
@Directive({
  selector: '[appPedidoHover]',
  standalone: true
})
export class PedidoHoverDirective {
  constructor(private el: ElementRef, private renderer: Renderer2) {}

  @HostListener('mouseenter') onEnter() {
    this.renderer.setStyle(this.el.nativeElement, 'background-color', 'rgba(0,0,0,0.03)');
    this.renderer.setStyle(this.el.nativeElement, 'transition', 'background-color 120ms ease');
  }

  @HostListener('mouseleave') onLeave() {
    this.renderer.removeStyle(this.el.nativeElement, 'background-color');
  }
}
