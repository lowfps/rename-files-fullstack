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
      <button class="primary" (click)="processAsync()" [disabled]="loading()">Procesar (asíncrono · SQS)</button>
      @if (run()) {
        <button (click)="reprocess()" [disabled]="loading()">Reprocesar (run #{{ run()!.id }})</button>
      }
    </div>

    @if (error()) { <p class="error-text">{{ error() }}</p> }
    @if (asyncStatus()) { <p class="muted">⏳ {{ asyncStatus() }}</p> }
    @if (loading() && !asyncStatus()) { <p class="muted">Procesando…</p> }

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
  asyncStatus = signal<string | null>(null);

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

  /**
   * Flujo asíncrono: encola en SQS (respuesta inmediata) y sondea las ejecuciones hasta que el
   * worker, por su cuenta, crea una nueva. Demuestra que la API no procesa en la petición HTTP.
   */
  processAsync() {
    this.loading.set(true);
    this.error.set(null);
    this.asyncStatus.set(null);
    // Línea base: id de la ejecución más reciente conocida antes de encolar.
    this.api.listRuns().subscribe({
      next: runs => {
        const baselineId = runs.length ? runs[0].id : 0;
        this.api.processAsync().subscribe({
          next: resp => {
            this.asyncStatus.set(`Encolado en SQS (job ${resp.jobId}). Esperando al worker…`);
            this.pollForNewRun(baselineId, Date.now() + 20000);
          },
          error: e => this.fail(e)
        });
      },
      error: e => this.fail(e)
    });
  }

  private pollForNewRun(baselineId: number, deadline: number) {
    this.api.listRuns().subscribe({
      next: runs => {
        if (runs.length && runs[0].id !== baselineId) {
          this.run.set(runs[0]);
          this.asyncStatus.set(`Procesado por el worker (ejecución #${runs[0].id}).`);
          this.loading.set(false);
        } else if (Date.now() > deadline) {
          this.asyncStatus.set(null);
          this.error.set('El worker no procesó el trabajo a tiempo.');
          this.loading.set(false);
        } else {
          setTimeout(() => this.pollForNewRun(baselineId, deadline), 1000);
        }
      },
      error: e => this.fail(e)
    });
  }

  private wrap<T>(obs: Observable<T>, onNext: (v: T) => void) {
    this.loading.set(true);
    this.error.set(null);
    this.asyncStatus.set(null);
    obs.subscribe({
      next: v => { onNext(v); this.loading.set(false); },
      error: e => this.fail(e)
    });
  }

  private fail(e: { error?: { detail?: string } }) {
    this.error.set(e?.error?.detail || 'Error en la petición');
    this.loading.set(false);
  }
}
