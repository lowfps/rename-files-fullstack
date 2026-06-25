# Evidencia de ejecución

Stack levantado con `docker compose up --build -d` (localstack, postgres, backend, frontend) y
validado end-to-end con el lote de muestra del reto.

## 1. Arranque del backend (logs)

```
Flyway: Migrating schema "public" to version "1 - schema"
Flyway: Migrating schema "public" to version "2 - seed rules"
Flyway: Successfully applied 2 migrations to schema "public", now at version v2
Hibernate ORM core version 7.4.1.Final
Tomcat started on port 8080 (http) with context path '/'
Started FilerenamerApplication in 9.803 seconds
```

## 2. Simulación de carga batch — `POST /api/files/seed`

```
PHO_CD_DES_20260430, PHO_SV_20260430, PHO_CK_20260430.txt, PHO_ML_UTIL_20260430.txt,
cuotas_bdb_20260430.txt, garantias_solo_firma_20260430.txt, activos_inmob_bdb_20260430.txt,
PrendasPajaro.txt
```

## 3. Procesamiento — `POST /api/process`

**Resumen (panel de control):**

| Total | Transformados | Errores | No mapeados |
|-------|----------------|---------|--------------|
| 8     | 7              | 0       | 1            |

**Detalle por archivo:**

| Archivo origen                    | Nombre transformado                          | Estado      | Regla |
|------------------------------------|-----------------------------------------------|-------------|-------|
| `PHO_CD_DES_20260430`               | `01_Estructura CDT Desmaterializado_20260430` | TRANSFORMED | 01    |
| `PHO_SV_20260430`                   | `03_Estructura Cuenta Ahorros_20260430`       | TRANSFORMED | 03    |
| `PHO_CK_20260430.txt`                | `04_Estructura Cuenta Corriente_20260430`     | TRANSFORMED | 04    |
| `PHO_ML_UTIL_20260430.txt`           | `13_CUOTAS Activos`                           | TRANSFORMED | 13    |
| `cuotas_bdb_20260430.txt`            | `13_CUOTAS Activos`                           | TRANSFORMED | 13    |
| `garantias_solo_firma_20260430.txt`  | `14_Hipotecaria`                              | TRANSFORMED | 14    |
| `activos_inmob_bdb_20260430.txt`     | `37_Leasing_Vehículo`                         | TRANSFORMED | 37    |
| `PrendasPajaro.txt`                  | —                                              | **NO_MAPEADO** | —  |

Confirma exactamente el comportamiento esperado por el reto: todos los patrones documentados se
transforman correctamente y el archivo sin patrón conocido (`PrendasPajaro.txt`) queda marcado como
**No mapeado**, sin romper el procesamiento del resto del lote.

> Nota: la primera captura mostró `VehÃ­culo` en lugar de `Vehículo` por un problema de decodificación
> de `Invoke-RestMethod` en PowerShell 5.1 (no del backend — verificado leyendo los bytes crudos de la
> respuesta, que ya venían en UTF-8 correcto). Se añadió `spring.servlet.encoding.*=UTF-8` de forma
> explícita para que cualquier cliente HTTP reciba el `charset` declarado.

## 4. Catálogo de reglas — `GET /api/rules`

| Código | Descripción                       | Patrón                              | Plantilla destino                            | Formato fecha |
|--------|------------------------------------|--------------------------------------|-----------------------------------------------|---------------|
| 01     | Estructura CDT Desmaterializado    | `^PHO_CD_DES_(?<date>\d{8})$`        | `01_Estructura CDT Desmaterializado_{date}`   | YYYYMMDD      |
| 03     | Estructura Cuenta Ahorros          | `^PHO_SV_(?<date>\d{8})$`            | `03_Estructura Cuenta Ahorros_{date}`         | YYYYMMDD      |
| 04     | Estructura Cuenta Corriente        | `^PHO_CK_(?<date>\d{8})$`            | `04_Estructura Cuenta Corriente_{date}`       | YYYYMMDD      |
| 13     | CUOTAS Activos (Mora/Utilizado)    | `^PHO_ML_UTIL_(?<date>\d{8})$`       | `13_CUOTAS Activos`                           | NONE          |
| 13     | CUOTAS Activos (BDB)               | `^cuotas_bdb_(?<date>\d{8})$`        | `13_CUOTAS Activos`                           | NONE          |
| 14     | Hipotecaria                        | `^garantias_.*$`                     | `14_Hipotecaria`                              | NONE          |
| 37     | Leasing Vehículo                   | `^activos_inmob_bdb_(?<date>\d{8})$` | `37_Leasing_Vehículo`                         | NONE          |

## 5. Procesamiento asíncrono con SQS — `POST /api/process/async`

Mismo motor de reglas, disparado por un segundo adaptador de entrada (worker SQS) en vez de por la
petición HTTP. El endpoint encola y responde de inmediato; el worker procesa por su cuenta.

```
Ejecuciones antes de encolar: 13 (última id=13)

POST /api/process/async  →  Respuesta HTTP inmediata en 9 ms:
  jobId  = 8055bb0b-04b8-474b-9acf-5564fd370d7d
  status = QUEUED

(el worker SQS procesa por su cuenta)
Nueva ejecución creada por el worker:
  id = 14 · total = 8 · transformados = 7 · errores = 0 · no mapeados = 1
```

**Traza del worker en los logs del backend:**

```
SqsProcessWorker : SQS → procesando trabajo 8055bb0b-... ({"type":"PROCESS_BUCKET","requestedAt":"..."})
SqsProcessWorker : SQS ← trabajo 8055bb0b-... completado: ejecución #14 (8 archivos)
```

La respuesta de 9 ms (frente al procesamiento inline) evidencia que el trabajo se ejecuta fuera de
la petición HTTP. El motor de reglas y el dominio son idénticos a la ruta síncrona: solo cambia el
adaptador que lo dispara.

## 6. Pruebas automatizadas

```
mvn test  →  Tests run: 22, Failures: 0, Errors: 0   (dominio + ArchUnit + encolado)
```

Cubre todos los ejemplos del reto (incluida la normalización `AAAADDMM`→`AAAAMMDD`, fecha inválida →
ERROR, resolución de reglas ambiguas por prioridad y reglas inactivas ignoradas) y la delegación del
servicio de encolado al puerto de cola. Los tests de integración `*IT` (`mvn verify`, requieren
Docker) levantan PostgreSQL + LocalStack S3/SQS reales con Testcontainers y verifican tanto el flujo
síncrono como el asíncrono (`ProcessAsyncIT`: encola y espera a que el worker cree la ejecución).
