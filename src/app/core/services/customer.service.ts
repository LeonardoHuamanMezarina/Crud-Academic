import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Customer } from '../interfaces/customer';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  constructor(private http: HttpClient, private auth: AuthService) { }
  private apiUrl = `${environment.urlBackEnd}/api/v1/user`;

  private authHeaders() {
    return { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  }

  findAll(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/listar/clientes`, { headers: this.authHeaders() });
  }

  delete(id: number): Observable<string> {
    return this.http.patch(`${this.apiUrl}/eliminar/${id}`, {}, { headers: this.authHeaders(), responseType: 'text' as 'text' });
  }

  save(customer: Partial<Customer>): Observable<Customer> {
    return this.http.post<Customer>(`${this.apiUrl}/crear`, customer, { headers: this.authHeaders() });
  }

  getById(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/listar/id/${id}`, { headers: this.authHeaders() });
  }

  update(customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/editar`, customer, { headers: this.authHeaders() });
  }

  restaurar(idUsuario: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/restaurar/${idUsuario}`, {}, { headers: this.authHeaders() });
  }

  reportPdf(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/pdf`, { headers: this.authHeaders(), responseType: 'blob' });
  }

  saveClient(customer: Partial<Customer>): Observable<Customer> {
    return this.http.post<Customer>(`${this.apiUrl}/crear/cliente`, customer, { headers: this.authHeaders() });
  }

  updateClient(customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/editar/cliente`, customer, { headers: this.authHeaders() });
  }
}
