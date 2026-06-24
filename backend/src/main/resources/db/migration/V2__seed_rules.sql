-- Catálogo inicial de reglas que refleja los ejemplos del reto.
INSERT INTO rename_rule (code, description, pattern, target_template, source_date_format, priority, active, version) VALUES
  ('01', 'Estructura CDT Desmaterializado', '^PHO_CD_DES_(?<date>\d{8})$', '01_Estructura CDT Desmaterializado_{date}', 'YYYYMMDD', 10, true, 1),
  ('03', 'Estructura Cuenta Ahorros',       '^PHO_SV_(?<date>\d{8})$',     '03_Estructura Cuenta Ahorros_{date}',       'YYYYMMDD', 20, true, 1),
  ('04', 'Estructura Cuenta Corriente',     '^PHO_CK_(?<date>\d{8})$',     '04_Estructura Cuenta Corriente_{date}',     'YYYYMMDD', 30, true, 1),
  ('13', 'CUOTAS Activos (Mora/Utilizado)', '^PHO_ML_UTIL_(?<date>\d{8})$', '13_CUOTAS Activos',                        'NONE',     40, true, 1),
  ('13', 'CUOTAS Activos (BDB)',            '^cuotas_bdb_(?<date>\d{8})$',  '13_CUOTAS Activos',                        'NONE',     41, true, 1),
  ('14', 'Hipotecaria',                     '^garantias_.*$',               '14_Hipotecaria',                           'NONE',     50, true, 1),
  ('37', 'Leasing Vehículo',                '^activos_inmob_bdb_(?<date>\d{8})$', '37_Leasing_Vehículo',                'NONE',     60, true, 1);

-- Snapshot inicial en el historial de versiones.
INSERT INTO rename_rule_version (rule_id, code, description, pattern, target_template, source_date_format, priority, active, version)
SELECT id, code, description, pattern, target_template, source_date_format, priority, active, version FROM rename_rule;
