# Arquitectura — Renombramiento Inteligente de Archivos en S3

## Vista de componentes (cloud / despliegue)

```mermaid
flowchart LR
    UI["Angular SPA<br/>(nginx)"] -- REST /api --> API["Spring Boot API<br/>(arquitectura hexagonal)"]
    API -- "encola (async)" --> SQS[("SQS / LocalStack<br/>process-jobs")]
    SQS -- "consume (worker)" --> API
    API -- AWS SDK v2 --> S3[("S3 / LocalStack<br/>bucket incoming-files")]
    API -- JDBC --> PG[("PostgreSQL<br/>reglas · versiones · resultados")]
    API --- FW["Flyway<br/>schema + seed"]
```

## Arquitectura hexagonal (Ports & Adapters) del backend

```mermaid
flowchart TB
    subgraph IN["Adaptadores de entrada"]
        WEB["REST Controllers<br/>RuleController · ProcessController · FilesController"]
        WORKER["SqsProcessWorker<br/>(consumidor asíncrono)"]
    end

    subgraph APP["Aplicación (casos de uso)"]
        PFS["ProcessFilesService"]
        EPS["EnqueueProcessService"]
        MRS["ManageRulesService"]
        QRS["QueryResultsService"]
        BKS["BucketService"]
    end

    subgraph DOM["Dominio (núcleo, sin frameworks)"]
        ENGINE["RuleEngine"]
        DATE["DateNormalizer"]
        TPL["TemplateRenderer"]
        MODEL["RenameRule · RenameResult · ProcessRun"]
        PIN["Puertos IN"]
        POUT["Puertos OUT"]
    end

    subgraph OUT["Adaptadores de salida"]
        JPA["RuleRepositoryAdapter<br/>ResultRepositoryAdapter (JPA)"]
        S3A["S3StorageAdapter (AWS SDK v2)"]
        SQSA["SqsJobQueueAdapter (AWS SDK v2)"]
    end

    WEB --> PIN
    WORKER --> PIN
    PIN -.implementan.- APP
    APP --> ENGINE
    APP --> POUT
    POUT -.implementan.- JPA
    POUT -.implementan.- S3A
    POUT -.implementan.- SQSA
    ENGINE --> DATE
    ENGINE --> TPL
    ENGINE --> MODEL

    style DOM fill:#eef7ee,stroke:#2e7d32
    style APP fill:#eef2fb,stroke:#1565c0
```

**Regla de dependencia** (verificada con ArchUnit en `HexagonalArchitectureTest`):
- `domain` no depende de Spring, AWS, JPA ni de `application`/`adapter`/`config`.
- `application` depende solo del `domain` (puertos), no de adaptadores ni de Spring.
- El cableado vive en `config.BeanConfiguration` (composition root).

## Flujo de transformación (motor de reglas)

```mermaid
flowchart TB
    A["Archivo del bucket"] --> B["Quitar extensión<br/>(soporta sin ext. y .txt)"]
    B --> C{"¿Casa alguna regla<br/>activa (por prioridad)?"}
    C -- No --> D["NO_MAPEADO"]
    C -- Sí --> E["Extraer grupos nombrados<br/>de la regex"]
    E --> F{"¿La regla declara fecha?"}
    F -- Sí --> G["DateNormalizer<br/>→ yyyyMMdd canónico"]
    F -- No --> H["TemplateRenderer"]
    G --> H
    H --> I{"¿Fecha inválida /<br/>plantilla incompleta?"}
    I -- Sí --> J["ERROR"]
    I -- No --> K["TRANSFORMED<br/>nombre estandarizado"]
```

## Procesamiento asíncrono (SQS)

El mismo caso de uso `ProcessFilesUseCase.process()` se dispara por **dos adaptadores de entrada**
distintos, sin que el dominio ni el motor de reglas cambien:

```mermaid
flowchart LR
    C["Cliente"] -- "POST /api/process/async" --> CTRL["ProcessController"]
    CTRL --> EPS["EnqueueProcessService"]
    EPS --> PORT["ProcessJobQueuePort"]
    PORT -.implementa.- SQSA["SqsJobQueueAdapter"]
    SQSA -- "sendMessage" --> Q[("SQS process-jobs")]
    Q -- "long-poll" --> W["SqsProcessWorker<br/>(@Scheduled)"]
    W --> PFU["ProcessFilesUseCase.process()"]
    PFU --> ENG["MISMO RuleEngine"]
```

- La petición HTTP responde `202 Accepted` con un `jobId` de inmediato (no procesa inline).
- El worker consume el mensaje, ejecuta el motor y persiste la ejecución; el panel la muestra al
  consultarla. Entrega *al menos una vez*: si el procesamiento falla, el mensaje no se borra y SQS
  lo reintenta tras el *visibility timeout*.
- Demostración de la arquitectura hexagonal: agregar un canal de entrada nuevo (SQS) **no tocó** el
  dominio; solo se añadieron un puerto, su adaptador y el worker.

## Modelo de datos (PostgreSQL)

- `rename_rule` — estado vigente de cada regla.
- `rename_rule_version` — snapshot inmutable por cada cambio (historial/auditoría).
- `ruleset_version` — contador global del catálogo (trazabilidad de reprocesos).
- `process_run` — ejecución (timestamp + versión de catálogo usada).
- `rename_result` — detalle por archivo (estado, regla aplicada, mensaje).
