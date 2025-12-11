import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { Pedido, SaleDetail } from '../interfaces/pedido';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  // usa la URL específica de pedidos si está definida, si no usa urlBackEnd
  // Por compatibilidad con el backend actual, apuntamos al controlador de ventas (/api/v1/sale)
  private apiUrl = environment.urlPedidosBackEnd ?? `${environment.urlBackEnd}/api/v1/sale`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  private authHeaders(): Record<string, string> | undefined {
    const token = this.auth.getAccessToken();
    if (!token) return undefined;
    const headers: Record<string, string> = { Authorization: `Bearer ${token}` };
    return headers;
  }

  // Helper para leer distintos formatos de campo (ID_SALE, id_sale, idSale)
  private pick(obj: any, keys: string[]) {
    for (const k of keys) {
      if (obj == null) continue;
      if (k in obj && obj[k] !== undefined && obj[k] !== null) return obj[k];
    }
    return undefined;
  }

  private mapDetail(dto: any): SaleDetail {
    return {
      idSaleDetail: this.pick(dto, ['idSaleDetail', 'ID_SALE_DETAIL', 'id_sale_detail', 'id_sale_detail']),
      unitPrice: this.pick(dto, ['unitPrice', 'UNIT_PRICE', 'unit_price']),
      quantity: this.pick(dto, ['quantity', 'QUANTITY']),
      subtotal: this.pick(dto, ['subtotal', 'SUBTOTAL']),
      registrationDate: this.pick(dto, ['registrationDate', 'REGISTRATION_DATE', 'registration_date']),
      productId: this.pick(dto, ['productId', 'PRODUCT_ID', 'product_id']),
      saleId: this.pick(dto, ['saleId', 'SALE_ID', 'sale_id'])
    };
  }

  private mapSale(dto: any): Pedido {
    if (!dto) return {} as Pedido;
    // Soporta varios nombres: 'details', 'saleDetails', 'SALE_DETAIL', 'sale_detail', 'products'
    const detailsArr = this.pick(dto, ['details', 'saleDetails', 'SALE_DETAIL', 'sale_detail', 'products', 'productsList']) || [];
    const details = Array.isArray(detailsArr) ? detailsArr.map(d => this.mapDetail(d)) : [];

      const id = this.pick(dto, ['saleId', 'idSale', 'ID_SALE', 'id_sale', 'ID_Sale']);
      const regDate = this.pick(dto, ['registrationDate', 'REGISTRATION_DATE', 'registration_date', 'saleDate']);
      const total = this.pick(dto, ['totalPayment', 'TOTAL_PAYMENT', 'total_payment', 'total']);

      // customer puede venir anidado como 'customer' { customerId: ..., firstName, lastName }
      const customerObj = this.pick(dto, ['customer', 'cliente', 'client']) || {};
      const clienteId = this.pick(customerObj, ['customerId', 'customer_id', 'customerID', 'idUsuario', 'id_usuario']) || this.pick(dto, ['userId', 'USER_ID', 'user_id']);

      // Nombre del cliente (intenta varios formatos posibles)
      const firstName = this.pick(customerObj, ['firstName', 'first_name', 'nombre', 'name']) || this.pick(dto, ['firstName', 'first_name', 'nombre', 'name']) || '';
      const lastName = this.pick(customerObj, ['lastName', 'last_name', 'apellido', 'surname']) || this.pick(dto, ['lastName', 'last_name', 'apellido', 'surname']) || '';
      const clienteNombre = (firstName || lastName) ? `${firstName} ${lastName}`.trim() : (this.pick(customerObj, ['customerName', 'name', 'nombre']) || this.pick(dto, ['customerName', 'name', 'nombre']));

      // Cantidad de productos en la venta (longitud de details)
      const productCount = Array.isArray(details) ? details.length : 0;

      return {
        idSale: id,
        // alias que usan las vistas
        idPedido: id,
        status: this.pick(dto, ['status', 'STATUS', 'state', 'STATE']),
        paymentType: this.pick(dto, ['paymentType', 'PAYMENT_TYPE', 'payment_type']),
        totalPayment: total,
        // alias para template
        total: total,
        receiptType: this.pick(dto, ['receiptType', 'RECEIPT_TYPE', 'receipt_type']),
        receiptNumber: this.pick(dto, ['receiptNumber', 'RECEIPT_NUMBER', 'receipt_number']),
        registrationDate: regDate,
        // alias para template
        fechaPedido: regDate,
        userId: this.pick(dto, ['userId', 'USER_ID', 'user_id']),
        clienteId: clienteId,
        clienteNombre: clienteNombre,
        productCount: productCount,
        deliveryId: this.pick(dto, ['deliveryId', 'DELIVERY_ID', 'delivery_id']),
        // detalles de la venta
        details
      };
  }

  findAll(): Observable<Pedido[]> {
    // El backend expone el listado completo en GET /api/v1/sale/all
    return this.http.get<any[]>(`${this.apiUrl}/all`, { headers: this.authHeaders() }).pipe(
      map(arr => (Array.isArray(arr) ? arr.map(a => this.mapSale(a)) : []))
    );
  }

  getById(id: number): Observable<Pedido> {
    // Backend: GET /api/v1/sale/{id}
    return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.authHeaders() }).pipe(
      map(dto => this.mapSale(dto))
    );
  }

  // ejemplo de creación/actualización; envía payload tal como el backend espera
  save(pedido: Partial<Pedido>): Observable<Pedido> {
    return this.http.post<any>(`${this.apiUrl}/crear`, pedido, { headers: this.authHeaders() }).pipe(
      map(dto => this.mapSale(dto))
    );
  }

  // Guarda una venta para un usuario específico: POST /api/v1/sale/save/{userId}
  saveForUser(userId: number, payload: any): Observable<Pedido> {
    const url = `${this.apiUrl}/save/${userId}`;
    return this.http.post<any>(url, payload, { headers: this.authHeaders() }).pipe(
      map(dto => this.mapSale(dto))
    );
  }

}
