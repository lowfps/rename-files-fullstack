import { Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { Rules } from './rules/rules';

export const routes: Routes = [
  { path: 'dashboard', component: Dashboard },
  { path: 'rules', component: Rules },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
];
