import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';

// componentes standalone
import { CustomerListComponent } from './customer-list/customer-list.component';
import { CustomerFormComponent } from './customer-form/customer-form.component';
import { CustomerEditComponent } from './customer-edit/customer-edit.component';

// guards / resolvers
import { AuthGuard } from '../../core/guards/auth.guard';
import { RoleGuard } from '../../core/guards/role.guard';
import { CustomersResolver } from '../../core/resolvers/customers.resolver';
import { PendingChangesGuard } from '../../core/guards/pending-changes.guard';

const routes: Routes = [
  {
    path: '',
    component: CustomerListComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'] },
    resolve: { customers: CustomersResolver }
  },
  {
    path: 'crear',
    component: CustomerFormComponent,
    canActivate: [AuthGuard, RoleGuard]
  },
  {
    path: 'editar/:id',
    component: CustomerEditComponent,
    canActivate: [AuthGuard, RoleGuard],
    canDeactivate: [PendingChangesGuard]
  }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    // importar componentes standalone
    CustomerListComponent,
    CustomerFormComponent,
    CustomerEditComponent
  ]
})
export class CustomerModule {}
