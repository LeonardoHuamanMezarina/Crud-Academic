import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'truncate' })
export class TruncatePipe implements PipeTransform {
  transform(value: string | null | undefined, limit = 40): string {
    if (!value) return '';
    const v = value.toString();
    if (v.length <= limit) return v;
    return v.slice(0, limit) + '...';
  }
}
