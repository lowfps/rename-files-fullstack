-- Catálogo de reglas (estado vigente)
CREATE TABLE rename_rule (
    id                 BIGSERIAL PRIMARY KEY,
    code               VARCHAR(32)   NOT NULL,
    description        VARCHAR(255)  NOT NULL,
    pattern            VARCHAR(512)  NOT NULL,
    target_template    VARCHAR(512)  NOT NULL,
    source_date_format VARCHAR(16)   NOT NULL,
    priority           INTEGER       NOT NULL,
    active             BOOLEAN       NOT NULL,
    version            INTEGER       NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_rename_rule_active ON rename_rule (active);

-- Historial inmutable de versiones de cada regla
CREATE TABLE rename_rule_version (
    id                 BIGSERIAL PRIMARY KEY,
    rule_id            BIGINT        NOT NULL,
    code               VARCHAR(32)   NOT NULL,
    description        VARCHAR(255)  NOT NULL,
    pattern            VARCHAR(512)  NOT NULL,
    target_template    VARCHAR(512)  NOT NULL,
    source_date_format VARCHAR(16)   NOT NULL,
    priority           INTEGER       NOT NULL,
    active             BOOLEAN       NOT NULL,
    version            INTEGER       NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_rename_rule_version_rule ON rename_rule_version (rule_id);

-- Contador global del catálogo (una sola fila)
CREATE TABLE ruleset_version (
    id      INTEGER PRIMARY KEY,
    version BIGINT  NOT NULL
);
INSERT INTO ruleset_version (id, version) VALUES (1, 1);

-- Ejecuciones del proceso
CREATE TABLE process_run (
    id              BIGSERIAL PRIMARY KEY,
    executed_at     TIMESTAMPTZ NOT NULL,
    ruleset_version BIGINT      NOT NULL
);

-- Detalle por archivo de cada ejecución
CREATE TABLE rename_result (
    id                   BIGSERIAL PRIMARY KEY,
    run_id               BIGINT       NOT NULL REFERENCES process_run (id) ON DELETE CASCADE,
    source_file_name     VARCHAR(512) NOT NULL,
    target_file_name     VARCHAR(512),
    status               VARCHAR(16)  NOT NULL,
    applied_rule_code    VARCHAR(32),
    applied_rule_version INTEGER,
    message              VARCHAR(512)
);

CREATE INDEX idx_rename_result_run ON rename_result (run_id);
