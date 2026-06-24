export type DateFormat = 'YYYYMMDD' | 'YYYYDDMM' | 'NONE';

export type RenameStatus = 'TRANSFORMED' | 'ERROR' | 'NO_MAPEADO';

export interface Rule {
  id: number;
  code: string;
  description: string;
  pattern: string;
  targetTemplate: string;
  sourceDateFormat: DateFormat;
  priority: number;
  active: boolean;
  version: number;
}

export interface RuleRequest {
  code: string;
  description: string;
  pattern: string;
  targetTemplate: string;
  sourceDateFormat: DateFormat;
  priority: number;
  active: boolean;
}

export interface RenameResult {
  sourceFileName: string;
  targetFileName: string | null;
  status: RenameStatus;
  appliedRuleCode: string | null;
  appliedRuleVersion: number | null;
  message: string | null;
}

export interface Summary {
  total: number;
  transformed: number;
  errors: number;
  noMapeado: number;
}

export interface ProcessRun {
  id: number;
  executedAt: string;
  rulesetVersion: number;
  summary: Summary;
  results: RenameResult[];
}
