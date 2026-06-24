import { Component, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import { ProcessRun } from '../models';

@Component({
  selector: 'app-dashboard',
  template: `
    <h2>Panel de control</h2>

    <div class="toolbar">
      <button class="accent" (click)="seed()" [disabled]="loading()">Sembrar lote en S3</button>
      <button (click)="loadFiles()" [disabled]="loading()">Listar archivos del bucket</button>
      <button class="primary" (click)="process()" [disabled]="loading()">Procesar</button>
      @if (run()) {
        <button (click)="reprocess()" [disabled]="loading()">Reprocesar (run #{{ run()!.id }})</button>
      }
    </div>

    @if (error()) { <p class="error-text">{{ error() }}</p> }
    @if (loading()) { <p class="muted">Procesando…</p> }

    @if (files().length) {
      <div class="panel">
        <strong>Archivos en el bucket ({{ files().length }}):</strong>
        <span class="mono"> {{ files().join(', ') }}</span>
      </div>
    }

    @if (run(); as r) {
      <div class="cards">
        <div class="card"><div class="label">Total procesados</div><div class="value">{{ r.summary.total }}</div></div>
        <div class="card ok"><div class="label">Transformados</div><div class="value">{{ r.summary.transformed }}</div></div>
        <div class="card err"><div class="label">Con error</div><div class="value">{{ r.summary.errors }}</div></div>
        <div class="card nomap"><div class="label">No mapeados</div><div class="value">{{ r.summary.noMapeado }}</div></div>
      </div>

      <p class="muted">Ejecución #{{ r.id }} · versión de catálogo: {{ r.rulesetVersion }} · {{ r.executedAt }}</p>

      <table>
        <thead>
          <tr>
            <th>Archivo origen</th>
            <th>Nombre transformado</th>
            <th>Estado</th>
            <th>Regla</th>
            <th>Detalle</th>
          </tr>
        </thead>
        <tbody>
          @for (res of r.results; track res.sourceFileName) {
            <tr>
              <td class="mono">{{ res.sourceFileName }}</td>
              <td class="mono">{{ res.targetFileName || '—' }}</td>
              <td><span class="badge {{ res.status }}">{{ res.status }}</span></td>
              <td>{{ res.appliedRuleCode || '—' }}</td>
              <td class="muted">{{ res.message }}</td>
            </tr>
          }
        </tbody>
      </table>
    } @else if (!loading()) {
      <p class="muted">Siembra el lote y pulsa «Procesar» para ver resultados.</p>
    }
  `
})
export class Dashboard {
  private api = inject(ApiService);

  files = signal<string[]>([]);
  run = signal<ProcessRun | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  seed() {
    this.wrap(this.api.seedFiles(), files => this.files.set(files));
  }

  loadFiles() {
    this.wrap(this.api.listFiles(), files => this.files.set(files));
  }

  process() {
    this.wrap(this.api.process(), run => this.run.set(run));
  }

  reprocess() {
    const id = this.run()!.id;
    this.wrap(this.api.reprocess(id), run => this.run.set(run));
  }

  private wrap<T>(obs: Observable<T>, onNext: (v: T) => void) {
    this.loading.set(true);
    this.error.set(null);
    obs.subscribe({
      next: v => { onNext(v); this.loading.set(false); },
      error: e => { this.error.set(e?.error?.detail || 'Error en la petición'); this.loading.set(false); }
    });
  }
}
