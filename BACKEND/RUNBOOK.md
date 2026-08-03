# CardPro AI — Build & Run Order

Chronological startup order for the CardPro AI microservices backend.

## Port Map

| Service            | Port |
|--------------------|------|
| Discovery (Eureka) | 8761 |
| Gateway            | 8765 |
| Auth               | 8081 |
| Card               | 8082 |
| Lead               | 8083 |
| AI                 | 8084 |
| Payment            | 8085 |
| Order              | 8086 |

## 1. Build everything (once)

From `BACKEND/` (JDK 17+, Maven 3.8+):

```bash
mvn clean install -DskipTests
```

## 2. Start infrastructure

PostgreSQL and Redis are required by most services. Two options:

**Option A — Docker (recommended):**

```bash
docker compose up -d postgres redis
```

`docker-compose.yml` creates these databases via `database/scripts/init-multiple-databases.sh`:
`auth_db, card_db, lead_db, payment_db, cardpro_orders`.

**Option B — Local installs:** run Postgres on `5432` and Redis on `6379`, then
create the databases above with credentials matching the service `application.yml` defaults.

## 3. Start services in order

### 3.1 Discovery Service (Eureka) — FIRST
```bash
cd BACKEND/discovery-service
mvn spring-boot:run
```
- Runs on **8761**. Eureka uses HTTP Basic auth: `eureka / eureka123`
  (configurable via `EUREKA_USERNAME` / `EUREKA_PASSWORD`).
  The dashboard (`/`) and registration endpoints are Basic-auth protected;
  `/actuator/health`, `/actuator/info` and `/actuator/metrics` are open so
  Docker/K8s probes and the compose `service_healthy` gate work.
- Verify it is up: `http://localhost:8761/actuator/health` (open) or
  `http://eureka:eureka123@localhost:8761/` (dashboard, Basic auth).
- **Wait for the dashboard to be reachable before starting anything else.**

### 3.2 Database-backed services — SECOND (any order)
Start each in its own terminal. All register with Eureka; make sure
`JWT_SECRET` and `INTERNAL_API_KEY` are exported in the shell
(or provided in your IDE run configuration).

```bash
cd BACKEND/auth-service    && mvn spring-boot:run   # 8081
cd BACKEND/card-service    && mvn spring-boot:run   # 8082
cd BACKEND/lead-service    && mvn spring-boot:run   # 8083
cd BACKEND/order-service   && mvn spring-boot:run   # 8086
```

Suggested order: **auth → card → lead → order** (card depends on auth for JWT
validation; lead depends on card; order calls product via Feign).
`payment-service` (8085) and `ai-service` (8084) can be added the same way if needed.

### 3.3 Gateway — LAST
```bash
cd BACKEND/gateway-service
mvn spring-boot:run   # 8765
```
- Start the Gateway **only after** Eureka is healthy — it routes via
  `lb://<SERVICE-NAME>` and needs the registry populated.
- Verify: `http://localhost:8765/actuator/health`, then hit a proxied route,
  e.g. `http://localhost:8765/api/v1/products`.

## Quick health checklist

| Service    | Health URL                                      | Expected |
|------------|-------------------------------------------------|----------|
| Eureka     | `http://localhost:8761/actuator/health`         | UP       |
| Gateway    | `http://localhost:8765/actuator/health`         | UP       |
| Card       | `http://localhost:8082/actuator/health`         | UP       |
| Lead       | `http://localhost:8083/actuator/health`         | UP       |
| Order      | `http://localhost:8086/actuator/health`         | UP       |

> Redis is **optional** for Gateway, Card, and AI: those services' Redis health
> indicators are disabled (`management.health.redis.enabled: false`), and the card
> cache degrades gracefully to cache-miss when Redis is unreachable. Health reports
> `UP` even without Redis running.

## Docker alternative (whole stack)

`docker compose up --build` starts everything with `depends_on` ordering
(Postgres → Redis → Discovery → services → Gateway → Frontend). Use the
`docker-compose.yml` environment variables as the single source of truth for
credentials and hosts.

## Env vars required

- `JWT_SECRET` — shared JWT signing secret (all services)
- `INTERNAL_API_KEY` — inter-service API key (card, lead, order, payment, ai)
- Optional overrides: `EUREKA_USERNAME` / `EUREKA_PASSWORD` (default `eureka` / `eureka123`)
