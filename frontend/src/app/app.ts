import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <h1>Renombramiento Inteligente de Archivos S3</h1>
      <nav>
        <a routerLink="/dashboard" routerLinkActive="active">Panel</a>
        <a routerLink="/rules" routerLinkActive="active">Reglas</a>
      </nav>
    </header>
    <main>
      <router-outlet />
    </main>
  `,
  styleUrl: './app.scss'
})
export class App {}
