import { Directive, Input, ElementRef, Renderer2, OnChanges, SimpleChanges } from '@angular/core';

/**
 * Shows a small badge or red highlight when product count is low.
 * Usage: <td [appLowStockBadge]="count" [threshold]="10"></td>
 */
@Directive({
  selector: '[appLowStockBadge]',
  standalone: true
})
export class LowStockBadgeDirective implements OnChanges {
  @Input('appLowStockBadge') count: number | string | null = 0;
  @Input() threshold = 10;

  private badgeEl?: HTMLElement;

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(changes: SimpleChanges) {
    this.update();
  }

  private parseCount(): number {
    const v = this.count ?? 0;
    if (typeof v === 'number') return v;
    const cleaned = String(v).replace(/[^0-9.-]+/g, '');
    const n = parseInt(cleaned, 10);
    return isFinite(n) ? n : 0;
  }

  private update() {
    const value = this.parseCount();
    // ensure the cell shows the numeric value
    this.renderer.setProperty(this.el.nativeElement, 'textContent', String(value));

    if (value <= this.threshold) {
      // add red background to the cell
      this.renderer.setStyle(this.el.nativeElement, 'background-color', '#f8d7da');
      this.renderer.setStyle(this.el.nativeElement, 'color', '#721c24');
      this.renderer.setStyle(this.el.nativeElement, 'font-weight', '700');
    } else {
      // clear styles
      this.renderer.removeStyle(this.el.nativeElement, 'background-color');
      this.renderer.removeStyle(this.el.nativeElement, 'color');
      this.renderer.removeStyle(this.el.nativeElement, 'font-weight');
    }
  }
}
