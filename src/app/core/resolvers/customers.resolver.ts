import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { Observable } from 'rxjs';
import { Customer } from '../interfaces/customer';
import { CustomerService } from '../services/customer.service';

@Injectable({ providedIn: 'root' })
export class CustomersResolver implements Resolve<Customer[] | null> {
  constructor(private customerService: CustomerService) {}

  resolve(): Observable<Customer[] | null> {
    return this.customerService.findAll();
  }
}
