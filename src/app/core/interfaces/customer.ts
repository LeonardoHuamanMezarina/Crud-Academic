export interface Customer {
    idUsuario: number;
    email: string;
    firstName: string;
    lastName: string;
    documentType: string;
    documentNumber: string;
    birthDate: Date;
    phone: number;
    status: string;
    roles: string;
    address: string;
    registrationDate: Date;
}