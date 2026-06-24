# Renombramiento Inteligente de Archivos en S3

Solución **Fullstack Cloud** que normaliza automáticamente los nombres de archivos que una entidad
financiera recibe en un bucket S3 desde múltiples sistemas origen, aplicando un **motor de reglas
configurable** y mostrando el resultado, los errores de mapeo y un panel de control.

- **Backend:** Java 21 · Spring Boot 4.1 · **arquitectura hexagonal** (Ports & Adapters)
- **Frontend:** Angular 20 (standalone + signals)
- **Persistencia:** PostgreSQL (catálogo de reglas, versionamiento y resultados) · Flyway
- **Cloud:** Amazon S3 vía **LocalStack** (AWS SDK v2); migrable a AWS Free Tier sin tocar el dominio
- **Infra:** Docker Compose (localstack · postgres · backend · frontend)

> Diseño priorizado sobre cantidad de features: lo central es la **claridad y flexibilidad del motor
> de reglas** y la **separación entre configuración y lógica**.

---

## 🚀 Ejecución rápida (Docker Compose)

Requisitos: Docker Desktop.

```bash
docker compose up --build
```

Esto levanta:

| Servicio   | URL                            | Descripción                              |
|------------|--------------------------------|------------------------------------------|
| frontend   | http://localhost               | UI Angular (nginx, proxy `/api`)         |
| backend    | http://localhost:8080          | API REST Spring Boot                     |
| postgres   | localhost:5432                 | Base de datos                            |
| localstack | http://localhost:4566          | S3 simulado (bucket `incoming-files`)    |

**Flujo de demostración:**
1. Abrir http://localhost → pestaña **Panel**.
2. **Sembrar lote en S3** (simulación de carga batch).
3. **Procesar**: el motor lee el bucket, aplica las reglas y muestra el panel de control + la tabla
   `origen → transformado · estado · regla`.
4. Pestaña **Reglas**: crear/editar/desactivar reglas, ver **historial de versiones**.
5. Volver al **Panel** y **Reprocesar** para ver el efecto del nuevo catálogo (trazado por versión).

---

## 🧠 Motor de reglas (núcleo)

Una regla es **100% configuración** (persistida, editable por UI y versionada). Escalar el sistema
es **añadir reglas, no reescribir código**.

| Campo              | Ejemplo                                        |
|--------------------|------------------------------------------------|
| `pattern` (regex)  | `^PHO_CD_DES_(?<date>\d{8})$`                   |
| `targetTemplate`   | `01_Estructura CDT Desmaterializado_{date}`     |
| `sourceDateFormat` | `YYYYMMDD` · `YYYYDDMM` · `NONE`                |
| `priority`         | menor = mayor preferencia (resuelve ambigüedad) |

Flujo por archivo (`RuleEngine`, dominio puro):

1. Se quita la extensión (soporta sin extensión y `.txt`).
2. Se evalúan las reglas **activas por prioridad**; la primera que casa gana.
3. Los grupos nombrados de la regex se inyectan en la plantilla; la fecha se normaliza a `yyyyMMdd`
   canónico (`DateNormalizer`).
4. Estado del resultado:
   - **TRANSFORMED** — nombre estandarizado generado.
   - **ERROR** — casó pero la fecha es inválida o la plantilla está incompleta.
   - **NO_MAPEADO** — ninguna regla aplica (ej. `PrendasPajaro.txt`).

### Catálogo semilla (Flyway `V2__seed_rules.sql`)

| Patrón origen                  | Destino                              |
|--------------------------------|--------------------------------------|
| `PHO_CD_DES_*`                 | `01_Estructura CDT Desmaterializado_{date}` |
| `PHO_SV_*`                     | `03_Estructura Cuenta Ahorros_{date}` |
| `PHO_CK_*`                     | `04_Estructura Cuenta Corriente_{date}` |
| `PHO_ML_UTIL_*` / `cuotas_bdb_*` | `13_CUOTAS Activos`                 |
| `garantias_*`                  | `14_Hipotecaria`                     |
| `activos_inmob_bdb_*`          | `37_Leasing_Vehículo`                |

---

## 🏛️ Arquitectura hexagonal

Diagramas completos en [`docs/architecture.md`](docs/architecture.md).

```
com.bank.filerenamer
├── domain            # núcleo SIN frameworks: model, service (RuleEngine…), port.in / port.out
├── application       # casos de uso que orquestan puertos (sin Spring)
├── adapter
│   ├── in.web        # Controllers + DTOs + manejo de errores
│   └── out
│       ├── persistence   # JPA (PostgreSQL): reglas, versiones, resultados
│       └── storage       # S3 (AWS SDK v2)
└── config            # composition root (cableado) + configuración S3/CORS
```

La **regla de dependencia** (el dominio no conoce Spring/AWS/JPA; la aplicación solo conoce el
dominio) se verifica automáticamente con **ArchUnit** en `HexagonalArchitectureTest`.

---

## 🔌 API REST

| Método | Ruta                              | Descripción                          |
|--------|-----------------------------------|--------------------------------------|
| GET    | `/api/files`                      | Listar archivos del bucket           |
| POST   | `/api/files/seed`                 | Sembrar lote de muestra (batch)      |
| POST   | `/api/process`                    | Procesar bucket → ejecución + resumen|
| POST   | `/api/process/{runId}/reprocess`  | Reprocesar con reglas vigentes       |
| GET    | `/api/runs` · `/api/runs/{id}`    | Ejecuciones / panel de control       |
| GET    | `/api/results?runId=`             | Detalle por archivo                  |
| GET/POST/PUT/DELETE | `/api/rules`         | CRUD de reglas (DELETE = desactivar) |
| GET    | `/api/rules/{id}/versions`        | Historial de versiones               |

---

## 🧪 Pruebas

```bash
cd backend
mvn test       # unitarias del dominio (motor de reglas) + ArchUnit  — rápidas, sin Docker
mvn verify     # + integración con Testcontainers (PostgreSQL + LocalStack)  — requiere Docker
```

- **Unit + ArchUnit** (`*Test`, surefire): cubren todos los ejemplos del reto y bordes (sin
  extensión, sin fecha, fecha inválida → ERROR, no mapeado, ambigüedad `AAAADDMM`, prioridad).
- **Integración** (`*IT`, failsafe): siembran S3 en LocalStack, migran PostgreSQL con Flyway y
  ejecutan el flujo completo.

> **Windows + Docker Desktop:** si Testcontainers no encuentra Docker (`Could not find a valid Docker
> environment`), exporta el pipe del contexto activo antes de `mvn verify`:
> `set DOCKER_HOST=npipe:////./pipe/dockerDesktopLinuxEngine`. En Linux/CI funciona sin ajustes.

---

## 💻 Desarrollo local (sin Docker para la app)

```bash
# Infra mínima
docker compose up -d postgres localstack

# Backend (usa los valores por defecto de application.properties)
cd backend && mvn spring-boot:run

# Frontend (proxy /api → localhost:8080)
cd frontend && npm install && npm start   # http://localhost:4200
```

---

## ☁️ AWS Free Tier (opcional)

Ver [`docs/aws-free-tier.md`](docs/aws-free-tier.md). Gracias al puerto `FileStoragePort` (AWS SDK
v2) y a la configuración por propiedades, pasar de LocalStack a S3 real solo cambia el endpoint y las
credenciales — el dominio no se toca. Backend → EC2 t2.micro / Elastic Beanstalk; base de datos →
RDS PostgreSQL Free Tier.

---

## 📂 Estructura del repositorio

```
.
├── backend/            # Spring Boot (hexagonal)
├── frontend/           # Angular 20
├── infra/localstack/   # init del bucket en LocalStack
├── docs/               # arquitectura, evidencia, notas AWS
├── docker-compose.yml
└── README.md
```
