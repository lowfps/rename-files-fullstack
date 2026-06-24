import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProcessRun, Rule, RuleRequest } from './models';

const BASE = '/api';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  // --- Bucket ---
  listFiles(): Observable<string[]> {
    return this.http.get<string[]>(`${BASE}/files`);
  }

  seedFiles(): Observable<string[]> {
    return this.http.post<string[]>(`${BASE}/files/seed`, {});
  }

  // --- Proceso ---
  process(): Observable<ProcessRun> {
    return this.http.post<ProcessRun>(`${BASE}/process`, {});
  }

  reprocess(runId: number): Observable<ProcessRun> {
    return this.http.post<ProcessRun>(`${BASE}/process/${runId}/reprocess`, {});
  }

  listRuns(): Observable<ProcessRun[]> {
    return this.http.get<ProcessRun[]>(`${BASE}/runs`);
  }

  // --- Reglas ---
  listRules(): Observable<Rule[]> {
    return this.http.get<Rule[]>(`${BASE}/rules`);
  }

  createRule(body: RuleRequest): Observable<Rule> {
    return this.http.post<Rule>(`${BASE}/rules`, body);
  }

  updateRule(id: number, body: RuleRequest): Observable<Rule> {
    return this.http.put<Rule>(`${BASE}/rules/${id}`, body);
  }

  deactivateRule(id: number): Observable<Rule> {
    return this.http.delete<Rule>(`${BASE}/rules/${id}`);
  }

  ruleVersions(id: number): Observable<Rule[]> {
    return this.http.get<Rule[]>(`${BASE}/rules/${id}/versions`);
  }
}
