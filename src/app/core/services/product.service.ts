// src/app/core/services/product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../interfaces/product'; // Ya tiene ruta relativa correcta
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private baseUrl = `${environment.urlBackEnd}/api/v1/product`;

  constructor(private http: HttpClient, private auth: AuthService) { }

  getProducts(): Observable<Product[]> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.get<Product[]>(`${this.baseUrl}/listar`, { headers });
  }

  getProductById(id: number): Observable<Product> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.get<Product>(`${this.baseUrl}/listar/id/${id}`, { headers });
  }


  getProductsByStatus(status: string): Observable<Product[]> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.get<Product[]>(`${this.baseUrl}/listar/estado/${status}`, { headers });
  }


  createProduct(product: Omit<Product, 'idProduct' | 'registrationDate' | 'status'>): Observable<Product> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.post<Product>(`${this.baseUrl}/crear`, product, { headers });
  }

 
  updateProduct(product: Product): Observable<Product> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.put<Product>(`${this.baseUrl}/editar`, product, { headers });
  }

 
  deleteProduct(id: number): Observable<Product> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.patch<Product>(`${this.baseUrl}/eliminar/${id}`, {}, { headers });
  }

  
  restoreProduct(id: number): Observable<Product> {
  const headers = { Authorization: `Bearer ${this.auth.getAccessToken() ?? ''}` };
  return this.http.patch<Product>(`${this.baseUrl}/restaurar/${id}`, {}, { headers });
  }

    reportPdf()
  {
    return this.http.get(`${this.baseUrl}/pdf`, { responseType: 'blob' });
  }

}
