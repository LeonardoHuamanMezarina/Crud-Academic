export interface Product {
    idProduct?: number;         // 'Long' en Java -> 'number' en TypeScript
    name: string;
    specification: string;
    description: string;
    category: string;
    unitPrice: number;          // 'Double' en Java -> 'number' en TypeScript
    stock: number;              // ¡CORREGIDO! 'int/Integer' en Java -> 'number' en TypeScript
    status: string;
    internalCode: string;
    registrationDate: string;   // 'LocalDateTime' en Java -> 'string' (ISO 8601) en TypeScript
    brandId: number;            // ¡CORREGIDO! 'int/Long' en Java -> 'number' en TypeScript
}
