import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../api.service';
import { DateFormat, Rule } from '../models';

@Component({
  selector: 'app-rules',
  imports: [ReactiveFormsModule],
  template: `
    <h2>Catálogo de reglas</h2>
    @if (error()) { <p class="error-text">{{ error() }}</p> }

    <div class="panel">
      <strong>{{ editingId() ? 'Editar regla #' + editingId() : 'Nueva regla' }}</strong>
      <form [formGroup]="form" (ngSubmit)="save()" class="form-grid" style="margin-top:12px">
        <label>Código<input formControlName="code" placeholder="01"></label>
        <label>Prioridad<input type="number" formControlName="priority"></label>
        <label class="full">Descripción<input formControlName="description" placeholder="Estructura CDT Desmaterializado"></label>
        <label class="full">Patrón (regex)<input class="mono" formControlName="pattern" placeholder="^PHO_CD_DES_(?<date>\\d{8})$"></label>
        <label class="full">Plantilla destino<input class="mono" formControlName="targetTemplate" placeholder="01_Estructura CDT Desmaterializado_{date}"></label>
        <label>Formato de fecha
          <select formControlName="sourceDateFormat">
            <option value="YYYYMMDD">YYYYMMDD</option>
            <option value="YYYYDDMM">YYYYDDMM</option>
            <option value="NONE">NONE</option>
          </select>
        </label>
        <label style="flex-direction:row; align-items:center; gap:8px; margin-top:22px">
          <input type="checkbox" formControlName="active"> Activa
        </label>
        <div class="full toolbar" style="margin:0">
          <button class="primary" type="submit" [disabled]="form.invalid">{{ editingId() ? 'Guardar' : 'Crear' }}</button>
          @if (editingId()) { <button type="button" (click)="resetForm()">Cancelar</button> }
        </div>
      </form>
    </div>

    <table>
      <thead>
        <tr>
          <th>Cód.</th><th>Descripción</th><th>Patrón</th><th>Destino</th>
          <th>Fecha</th><th>Prio.</th><th>Estado</th><th>Ver.</th><th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        @for (r of rules(); track r.id) {
          <tr>
            <td>{{ r.code }}</td>
            <td>{{ r.description }}</td>
            <td class="mono">{{ r.pattern }}</td>
            <td class="mono">{{ r.targetTemplate }}</td>
            <td>{{ r.sourceDateFormat }}</td>
            <td>{{ r.priority }}</td>
            <td><span class="badge {{ r.active ? 'on' : 'off' }}">{{ r.active ? 'activa' : 'inactiva' }}</span></td>
            <td>v{{ r.version }}</td>
            <td>
              <button class="ghost" (click)="edit(r)">Editar</button>
              <button class="ghost" (click)="versions(r)">Versiones</button>
              @if (r.active) { <button class="ghost" (click)="deactivate(r)">Desactivar</button> }
            </td>
          </tr>
        }
      </tbody>
    </table>

    @if (versionsOf(); as v) {
      <div class="panel" style="margin-top:20px">
        <strong>Historial de versiones — regla #{{ v.ruleId }}</strong>
        <button class="ghost" (click)="versionsOf.set(null)">cerrar</button>
        <table style="margin-top:10px">
          <thead><tr><th>Versión</th><th>Descripción</th><th>Patrón</th><th>Destino</th><th>Activa</th></tr></thead>
          <tbody>
            @for (h of v.history; track h.version) {
              <tr>
                <td>v{{ h.version }}</td><td>{{ h.description }}</td>
                <td class="mono">{{ h.pattern }}</td><td class="mono">{{ h.targetTemplate }}</td>
                <td>{{ h.active ? 'sí' : 'no' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `
})
export class Rules {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);

  rules = signal<Rule[]>([]);
  editingId = signal<number | null>(null);
  versionsOf = signal<{ ruleId: number; history: Rule[] } | null>(null);
  error = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    code: ['', Validators.required],
    description: ['', Validators.required],
    pattern: ['', Validators.required],
    targetTemplate: ['', Validators.required],
    sourceDateFormat: this.fb.nonNullable.control<DateFormat>('NONE', Validators.required),
    priority: [100, Validators.required],
    active: [true]
  });

  constructor() {
    this.load();
  }

  load() {
    this.api.listRules().subscribe({
      next: rules => this.rules.set(rules),
      error: e => this.error.set(e?.error?.detail || 'No se pudieron cargar las reglas')
    });
  }

  save() {
    const body = this.form.getRawValue();
    const id = this.editingId();
    const req = id ? this.api.updateRule(id, body) : this.api.createRule(body);
    req.subscribe({
      next: () => { this.resetForm(); this.load(); },
      error: e => this.error.set(e?.error?.detail || 'No se pudo guardar la regla')
    });
  }

  edit(r: Rule) {
    this.editingId.set(r.id);
    this.form.setValue({
      code: r.code, description: r.description, pattern: r.pattern,
      targetTemplate: r.targetTemplate, sourceDateFormat: r.sourceDateFormat,
      priority: r.priority, active: r.active
    });
  }

  deactivate(r: Rule) {
    this.api.deactivateRule(r.id).subscribe({ next: () => this.load() });
  }

  versions(r: Rule) {
    this.api.ruleVersions(r.id).subscribe({
      next: history => this.versionsOf.set({ ruleId: r.id, history })
    });
  }

  resetForm() {
    this.editingId.set(null);
    this.form.reset({ code: '', description: '', pattern: '', targetTemplate: '', sourceDateFormat: 'NONE', priority: 100, active: true });
  }
}
