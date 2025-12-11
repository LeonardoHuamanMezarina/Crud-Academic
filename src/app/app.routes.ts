import { Routes } from '@angular/router';


//new import
import { RoleGuard } from './core/guards/role.guard';
import { AuthGuard } from './core/guards/auth.guard';
import { CustomersResolver } from './core/resolvers/customers.resolver';
import { PendingChangesGuard } from './core/guards/pending-changes.guard';

export const routes: Routes = [

    { path: 'login', loadComponent: () => import('./feature/auth/login/login.component').then(m => m.LoginComponent) },
    { path: 'register', loadComponent: () => import('./feature/auth/register/register.component').then(m => m.RegisterComponent) },
    // backward-compatible redirect: old path -> new lazy module path
    { path: 'customer-list', redirectTo: 'clientes', pathMatch: 'full' },
    // customer feature loaded as a module (lazy)
    { path: 'clientes', loadChildren: () => import('./feature/customer/customer.module').then(m => m.CustomerModule) },
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login'
    },
    { path: 'product-list', loadComponent: () => import('./feature/product/product-list/product-list.component').then(m => m.ProductListComponent), canActivate: [AuthGuard] },
    // pedidos (orders) feature
    { path: 'pedidos', loadChildren: () => import('./feature/pedido/pedido.module').then(m => m.PedidoModule), canActivate: [AuthGuard] },
    { path: 'header', loadComponent: () => import('./layout/header/header.component').then(m => m.HeaderComponent), canActivate: [AuthGuard] },
    { path: 'product-form', loadComponent: () => import('./feature/product/product-form/product-form.component').then(m => m.ProductFormComponent), canActivate: [AuthGuard] },

];
