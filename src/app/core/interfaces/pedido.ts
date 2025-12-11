// Interfaz que refleja las tablas SALE y SALE_DETAIL de tu backend.
// Incluye los campos más usados en UI. Marca opcionales los que no sean
// obligatorios en todas las respuestas.
export interface SaleDetail {
  idSaleDetail?: number;
  unitPrice?: number;
  quantity?: number;
  subtotal?: number;
  registrationDate?: string; // ISO string
  productId?: number;
  saleId?: number;
  // Campos opcionales que pueden venir en la respuesta del backend
  name?: string;
  productName?: string;
  description?: string;
}

export interface Pedido {
  idSale?: number;
  // Alias usados por las vistas / mapeos
  idPedido?: number;
  idPedidoAlias?: number;
  status?: string; // e.g. 'P' (pendiente), 'C' (completado)
  paymentType?: string;
  totalPayment?: number;
  // alias para template
  total?: number;
  receiptType?: string;
  receiptNumber?: string;
  registrationDate?: string; // ISO string
  // alias para template
  fechaPedido?: string;
  userId?: number;
  clienteId?: number;
  // Nombre completo del cliente para mostrar en la tabla
  clienteNombre?: string;
  // Cantidad total de productos en la venta
  productCount?: number;
  // Algunas respuestas pueden devolver la lista bajo la clave 'products'
  products?: SaleDetail[];
  deliveryId?: number;
  // detalles de la venta
  details?: SaleDetail[];
}

// Alias antiguo (si quieres mantener compatibilidad con código preexistente)
export type Sale = Pedido;

