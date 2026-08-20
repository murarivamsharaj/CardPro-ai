<div align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3"/>
  <img src="https://img.shields.io/badge/Spring_Cloud-2023.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cloud"/>
  <img src="https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React 18"/>
  <img src="https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript"/>
  <img src="https://img.shields.io/badge/Vite-5-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite"/>
  <img src="https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind CSS"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 15"/>
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis 7"/>
  <img src="https://img.shields.io/badge/RabbitMQ-3-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white" alt="RabbitMQ"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <br/>
  <img src="https://img.shields.io/badge/Microservices-Spring_Cloud-6DB33F?style=flat-square" alt="Microservices"/>
  <img src="https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=json-web-tokens" alt="JWT"/>
  <img src="https://img.shields.io/badge/Razorpay-Payments-02042B?style=flat-square&logo=razorpay" alt="Razorpay"/>
  <img src="https://img.shields.io/badge/OpenAI-GPT-412991?style=flat-square&logo=openai" alt="OpenAI"/>
  <img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="MIT License"/>
</div>

<br/>

<div align="center">
  <h1>🚀 CardPro AI</h1>
  <h3>AI-Powered Digital Visiting Card & Lead Generation SaaS Platform</h3>
  <p><em>Replace physical business cards with intelligent, lead-generating digital profiles — powered by AI.</em></p>
  <p><strong>🔗 Live Demo:</strong> <a href="https://frontend-two-kohl-2uyfslpb5o.vercel.app">frontend-two-kohl-2uyfslpb5o.vercel.app</a></p>
</div>

<br/>

---

## 📋 Table of Contents

- [Project Description](#-project-description)
- [Architecture](#-architecture)
  - [Core Components](#-core-components)
  - [Microservices Overview](#-microservices-overview)
  - [Event-Driven Messaging (RabbitMQ)](#-event-driven-messaging-rabbitmq)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Folder Structure](#-folder-structure)
- [Getting Started](#-getting-started)
- [Running the Backend](#-running-the-backend)
- [Running the Frontend](#-running-the-frontend)
- [Docker](#-docker)
- [API Documentation](#-api-documentation)
- [Future Scope](#-future-scope)
- [Contributing](#-contributing)
- [License](#-license)

---

## 📖 Project Description

**CardPro AI** is a high-performance **micro-SaaS** platform that revolutionizes professional networking by replacing traditional business cards with intelligent, AI-powered digital profiles that actively generate leads.

Built as a **production-grade, distributed microservices application**, CardPro AI is decomposed into independently deployable Spring Boot services that communicate over REST (synchronous), Redis (caching / rate limiting), and RabbitMQ (asynchronous events). Every service owns its own PostgreSQL database, registers with a Eureka discovery server, and is reached exclusively through a Spring Cloud Gateway edge.

While competitors offer static digital card links behind expensive monthly subscriptions, CardPro AI disrupts the market using a **freemium + microtransaction model** — the core software is completely free. Revenue is generated through high-value, one-time microtransactions:

| Product | Type | Price (INR) |
|---|---|---|
| 💳 Core Digital Card | Free | ₹0 |
| 🏆 Premium Templates | One-Time | ₹149 |
| 📡 NFC Smart Card (Hardware) | One-Time | ₹999 |
| 🔗 Custom Domain | One-Time | ₹499 |
| 📋 Lead Pack (100 Credits) | Consumable | ₹199 |
| 🖼️ AI Photo Upscale | One-Time | ₹49 |

### Why CardPro AI?

- ✅ **Zero Monthly Subscriptions** — Pay only for what you need, once
- ✅ **AI-Native Features** — Bio generation, photo enhancement, lead follow-ups
- ✅ **Lead Generation Engine** — Capture visitor details every time your card is viewed
- ✅ **Mobile-First Design** — Beautiful rendering on every device
- ✅ **Real-Time Analytics** — Track views, clicks, and leads

---

## 📚 Documentation

Detailed documentation is available in the [DOCS](./DOCS) directory:

| Document | Description |
|---|---|
| [📄 SRS v2.0](./DOCS/SRS_CardPro_AI_v2.0.md) | Complete Software Requirements Specification |
| [🏗️ Architecture v1.0](./DOCS/CardPro_AI_Microservices_Architecture_v1.0.md) | Microservices architecture design document (per-service deep dive) |
| [📁 Project Structure](./DOCS/CardPro_AI_Complete_Project_Folder_Structure.md) | Complete project folder structure |
| [🔧 Backend Runbook](./BACKEND/RUNBOOK.md) | Build & startup order for the backend |

---

## 🏗️ Architecture

The platform follows a **microservices architecture** using Spring Cloud, with each service owning its data and scaling independently. All external traffic enters through a single gateway, which routes to services discovered dynamically via Eureka.

```text
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                               │
│                                                                     │
│  ┌──────────────────────┐         ┌──────────────────────────┐      │
│  │   React SPA (Vite)   │         │   Mobile Browser         │      │
│  │   - Admin Dashboard  │         │   - Public Card Viewer   │      │
│  │   - Auth Pages       │         │   - Lead Form / vCard    │      │
│  │   - Store/Upgrades   │         │   - QR Scanner           │      │
│  └──────────┬───────────┘         └─────────────┬────────────┘      │
│             │                                   │                   │
│             └──────────────┬────────────────────┘                   │
│                            │ HTTPS                                  │
└────────────────────────────┼────────────────────────────────────────┘
                             │
┌────────────────────────────┼────────────────────────────────────────┐
│              SPRING CLOUD GATEWAY (Port 8765)                       │
│          JWT Validation | Rate Limiting | Route Routing             │
│                  (discovers services via Eureka)                    │
├────────────────────────────┼────────────────────────────────────────┤
│                    │                   │              │             │
│              ┌─────┴─────┐     ┌───────┴──────┐   ┌───┴───────┐     │
│              │ Auth Svc  │     │  Card Svc    │   │ Lead Svc  │     │
│              │ (8081)    │     │  (8082)      │   │ (8083)    │     │
│              └─────┬─────┘     └───────┬──────┘   └────┬──────┘     │
│                    │                   │               │            │
│              ┌─────┴─────┐     ┌───────┴──────┐   ┌────┴─────┐      │
│              │  AI Svc   │     │ Payment Svc  │   │ Order Svc│      │
│              │  (8084)   │     │  (8085)      │   │ (8086)   │      │
│              └─────┬─────┘     └───────┬──────┘   └────┬─────┘      │
│                    │                   │  ▲             │            │
│                    │                   │  │ RabbitMQ    │ publish    │
│                    │                   │  └──◀──────────┘ events     │
├────────────────────┼───────────────────┼───────────────┼────────────┤
│                    ▼                   ▼               ▼            │
│        ┌──────────────────────────────────────────────────────┐     │
│        │  PostgreSQL (per-service DBs) | Redis | RabbitMQ     │     │
│        │  Eureka (service registry)                           │     │
│        └──────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────┘
```

### 🔍 Core Components

#### 🧭 Eureka Discovery Server (`discovery-service`, :8761)

The service registry at the heart of the platform. Every microservice registers itself with Eureka on startup and sends heartbeats to maintain its presence. The gateway and inter-service Feign clients resolve service locations dynamically instead of hardcoding IP addresses, so instances can scale up/down without configuration changes.

- **Responsibilities:** registration, heartbeat monitoring, lease expiry (30s), instance lookup for load-balanced routing
- **State:** stateless (in-memory registry, no database)
- **Dashboard:** `http://localhost:8761` (Basic auth: `eureka` / `eureka123`)
- **Health:** `http://localhost:8761/actuator/health`

#### 🚪 Spring Cloud Gateway (`gateway-service`, :8765)

The **single entry point** for all external traffic — the only service exposed to the public internet. It routes requests to downstream services using `lb://` URIs resolved through Eureka.

- **JWT validation at the edge** — validates tokens for protected routes, extracts `userId` / `email` / `roles`, and injects them as headers to downstream services
- **Rate limiting** — Redis-backed `RequestRateLimiter` per IP for public endpoints (auth, public profile views)
- **CORS & correlation IDs** — allows the React frontend origin; injects `X-Correlation-Id` for distributed tracing
- **Webhook passthrough** — Razorpay webhooks bypass JWT (validated by signature in payment-service)

#### 🔐 Auth Service (`auth-service`, :8081)

Central identity and access control for the platform.

- User registration (unique email, BCrypt password hashing) and login
- JWT access-token (24h) + refresh-token (30d) issuance
- **Token blacklisting** in Redis on logout
- **Internal API** (`/api/v1/auth/internal/**`) protected by `INTERNAL_API_KEY`, used by other services to validate tokens and fetch user details
- Owns the `users` table in `auth_db` (Flyway-managed)

#### 🪪 Card Service (`card-service`, :8082)

The most traffic-intensive service — it serves read-heavy public profile views plus authenticated CRUD.

- Card profile CRUD with **JSONB** content (bio, skills, services, portfolio, testimonials, gallery, social links)
- **Redis cache-aside** pattern for public profile reads (`profile:{slug}`) with graceful cache-miss fallback
- Unique slug management, async view counters, template management (incl. premium template unlocking)
- Owns `card_profiles` in `card_db`; internal endpoints for lead/payment services

#### 🐇 RabbitMQ (Async Messaging)

RabbitMQ decouples order and payment processing so services remain independently scalable. `order-service` publishes domain events; `payment-service` consumes them asynchronously (Spring AMQP).

- **Exchange:** direct exchange (`order.exchange`) with a durable `order.created` queue
- **Flow:** order created → `OrderCreatedEvent` published → payment-service consumes → Razorpay order created → webhook verification → fulfillment (unlock template / refill lead credits)
- **Management UI:** `http://localhost:15672` (user: `cardpro` / pass: `cardpro`) when running the backend stack

#### 🗄️ PostgreSQL 15

One database **per service** — a strict data-ownership boundary enforced by the architecture.

| Database | Owned by |
|---|---|
| `auth_db` | auth-service (`users`) |
| `card_db` | card-service (`card_profiles`) |
| `lead_db` | lead-service (`leads`) |
| `payment_db` | payment-service (transactions) |
| `cardpro_orders` | order-service (orders) |
| `user_db`, `product_db` | user-service, product-service (catalog stack) |

All databases are created automatically on first boot by [`DATABASE/scripts/init-multiple-databases.sh`](./DATABASE/scripts/init-multiple-databases.sh); schema migrations are managed with **Flyway** (`ddl-auto: validate`).

#### ⚡ Redis 7

Shared, multi-purpose infrastructure layer:

- **Caching** — cache-aside for public card profiles
- **Rate limiting** — Redis-backed gateway rate limiter
- **Token blacklist** — invalidated JWTs on logout (auth-service)
- **Async events** — Redis Streams for view counters / AI follow-up pipeline

### 🧩 Microservices Overview

| Service | Port | Database | Responsibility |
|---|---|---|---|
| 🔍 **discovery-service** | `8761` | None | Eureka Service Registry |
| 🚪 **gateway-service** | `8765` | Redis | Edge gateway — JWT, rate limiting, routing |
| 🔐 **auth-service** | `8081` | `auth_db` | Registration, login, JWT issuance/blacklist |
| 🪪 **card-service** | `8082` | `card_db` + Redis | Card profile CRUD, caching, public viewer |
| 📋 **lead-service** | `8083` | `lead_db` | Lead capture, credit management, event publishing |
| 🤖 **ai-service** | `8084` | None (stateless) | OpenAI bio gen, photo upscaling, follow-ups |
| 💳 **payment-service** | `8085` | `payment_db` | Razorpay orders, webhook verification, RabbitMQ consumer |
| 🛒 **order-service** | `8086` | `cardpro_orders` | Order lifecycle, RabbitMQ producer |

**Supporting catalog services** (included in the backend stack, `BACKEND/docker-compose.yml`):

| Service | Responsibility |
|---|---|
| 👤 **user-service** | User profile CRUD (`/api/users`) (Port: 8087) |
| 📦 **product-service** | Product catalog CRUD (`/api/v1/products`) (Port: 8088) |

### 🔁 Event-Driven Messaging (RabbitMQ)

```text
┌──────────────────────┐      publish       ┌──────────────────────────┐
│     order-service    │  OrderCreatedEvent │        RabbitMQ          │
│  (order lifecycle)   │ ─────────────────▶ │  order.exchange          │
└──────────────────────┘                    │  └─▶ order.created queue │
                                            └────────────┬─────────────┘
                                                         │ consume (async)
                                            ┌────────────▼─────────────┐
                                            │     payment-service      │
                                            │  create Razorpay order   │
                                            │  verify webhook          │
                                            │  fulfillment (unlock)    │
                                            └──────────────────────────┘
```

This pattern gives **eventual consistency**: if payment-service is down, events remain queued and are processed when it recovers — no order is lost and no synchronous coupling is introduced.

### Key Architectural Decisions

| Decision | Implementation |
|---|---|
| **Service Discovery** | Eureka Server — all services register dynamically |
| **API Gateway** | Spring Cloud Gateway — single entry point, JWT validation at edge |
| **Authentication** | JWT tokens validated by Gateway; user info propagated via headers |
| **Database per Service** | Each microservice owns its PostgreSQL schema — no cross-service queries |
| **Caching** | Redis cache-aside pattern for public profile reads (90%+ hit ratio) |
| **Inter-Service** | OpenFeign clients with internal API key authentication |
| **Async Events** | RabbitMQ for order → payment; Redis Streams for lead → AI follow-up pipeline |
| **Schema Management** | Flyway migrations with `ddl-auto: validate` |
| **Observability** | Actuator health endpoints + Micrometer metrics (Prometheus/Grafana) |

---

## ✨ Features

### Core Features

| Feature | Description |
|---|---|
| 📄 **Dynamic Public Profile** | Name, Bio, Phone, Skills, Portfolio, Services, Testimonials, Gallery |
| 🔗 **Integrated Social Links** | LinkedIn, GitHub, YouTube, Instagram, and more |
| 📞 **Click-to-Call / WhatsApp** | One-tap contact with proper URI schemes |
| 📅 **Appointment Booking** | Integrated booking link support |
| 📍 **Google Maps Routing** | Deep-link to user's location |
| 💳 **UPI Payment QR** | Scannable UPI QR codes for instant payments |
| 💾 **Native Contact Save (.vcf)** | Client-side vCard generation |
| 📊 **Analytics Dashboard** | View counts, link clicks, lead metrics |
| ✏️ **Admin Live Editor** | Split-screen editor with real-time mobile preview |

### 🤖 AI-Native Integrations

| Feature | Description | Tech |
|---|---|---|
| ✍️ **AI Bio Generation** | Convert rough notes into polished professional bios | OpenAI GPT |
| 🖼️ **AI Photo Enhancement** | Background removal + 4K upscaling | Remove.bg + ESRGAN |
| 💬 **AI Lead Follow-ups** | Auto-generated WhatsApp message templates | OpenAI GPT |

### 💰 Monetization

| Feature | Model |
|---|---|
| Core Card Creation | ✅ Free |
| Premium Templates | 💵 One-Time (₹149) |
| NFC Smart Card | 💵 One-Time (₹999) |
| Custom Domain | 💵 One-Time (₹499) |
| Lead Credits (100 pack) | 💵 Refillable (₹199) |
| AI Photo Upscale | 💵 One-Time (₹49) |

---

## 🛠️ Technology Stack

### Backend

| Technology | Purpose |
|---|---|
| **Java 21** | Core language with records, pattern matching, virtual threads |
| **Spring Boot 3.3** | Production-grade microservices framework |
| **Spring Cloud 2023.0.2** | Service discovery, gateway, load-balanced clients |
| **Spring Cloud Gateway** | Reactive API Gateway with Eureka-based routing |
| **Spring Security 6** | JWT authentication, role-based authorization |
| **Spring Data JPA** | ORM with Hibernate 6 |
| **Spring AMQP (RabbitMQ)** | Asynchronous event-driven messaging (order → payment) |
| **PostgreSQL 15** | Primary database with JSONB support |
| **Redis 7** | Caching, rate limiting, token blacklist, async events |
| **Flyway** | Versioned database migrations |
| **OpenFeign** | Declarative HTTP clients for inter-service calls |
| **Resilience4j** | Circuit breaker for external AI API calls |
| **Razorpay SDK** | Payment gateway integration |
| **springdoc OpenAPI** | Swagger UI / OpenAPI docs per service |
| **Lombok** | Boilerplate reduction |
| **Micrometer** | Metrics and monitoring (Prometheus) |

### Frontend

| Technology | Purpose |
|---|---|
| **React 18** | UI framework with hooks and suspense |
| **TypeScript 5** | Type-safe JavaScript |
| **Vite 5** | Fast build tool and HMR dev server |
| **Tailwind CSS 3** | Utility-first styling |
| **Redux Toolkit** | Predictable state management |
| **React Router 6** | Client-side routing |
| **React Hook Form** | Performant form management |
| **Axios** | HTTP client with interceptors |
| **Recharts** | Analytics dashboard charts |
| **react-hot-toast** | Toast notifications |
| **qrcode.react** | QR code generation |
| **vcard-creator** | vCard (.vcf) file creation |

### DevOps

| Tool | Purpose |
|---|---|
| **Docker & Compose** | Containerization and local orchestration |
| **GitHub Actions** | CI/CD pipelines |
| **Prometheus + Grafana** | Monitoring and alerting |
| **Nginx** | Reverse proxy and SSL termination (frontend container) |

---

## 📁 Folder Structure

```text
cardpro-ai/
│
├── .github/                        # CI/CD workflows, issue/PR templates
├── architecture/                   # Architecture documentation & diagrams
├── BACKEND/                        # Spring Boot microservices (parent POM)
│   ├── discovery-service/          # Eureka Server (8761)
│   ├── gateway-service/            # Spring Cloud Gateway (8765)
│   ├── auth-service/               # Authentication & JWT (8081)
│   ├── card-service/               # Card Profiles (8082)
│   ├── lead-service/               # Lead Capture (8083)
│   ├── ai-service/                 # AI Integrations (8084)
│   ├── payment-service/            # Razorpay Payments (8085)
│   ├── order-service/              # Orders + RabbitMQ producer (8086)
│   ├── user-service/               # User profiles (catalog stack)
│   ├── product-service/            # Product catalog (catalog stack)
│   ├── docker-compose.yml          # Backend-only stack (incl. RabbitMQ)
│   ├── RUNBOOK.md                  # Build & run order
│   └── pom.xml                     # Parent POM
│
├── FRONTEND/                       # React + TypeScript + Vite + Tailwind
│   ├── src/
│   │   ├── api/                    # Axios instance & interceptors
│   │   ├── components/             # UI components (common/, dashboard/)
│   │   ├── context/                # React context providers
│   │   ├── hooks/                  # Custom hooks
│   │   ├── layouts/                # Layout components
│   │   ├── pages/                  # Route pages (admin, auth, dashboard, public)
│   │   ├── routes/                 # Router configuration & guards
│   │   ├── services/               # API service layer
│   │   ├── store/                  # Redux / Zustand state
│   │   ├── styles/                 # Global styles & Tailwind setup
│   │   ├── types/                  # Shared TypeScript types
│   │   └── utils/                  # vCard, QR, formatters
│   ├── package.json
│   ├── tailwind.config.js
│   └── vite.config.ts              # Dev server :5173, proxies /api → :8765
│
├── DATABASE/                       # SQL schemas, migrations, seeds, init scripts
├── DOCKER/                         # Docker configs, Nginx, monitoring, prod
├── DOCS/                           # SRS, Architecture, Folder-structure docs
├── POSTMAN/                        # API collections & environments
├── docker-compose.yml              # Full-stack quick start (root)
└── .env.example                    # Environment variable template
```

### Microservice Package Structure (Example: auth-service)

```text
auth-service/
├── pom.xml
├── Dockerfile
└── src/main/java/com/cardpro/auth/
    ├── AuthServiceApplication.java
    ├── config/          # SecurityConfig, RedisConfig
    ├── controller/      # AuthController, InternalAuthController
    ├── dto/             # request/, response/
    ├── entity/          # User.java
    ├── repository/      # UserRepository.java
    ├── service/         # AuthService, JwtService, TokenBlacklistService
    ├── security/        # JwtAuthenticationFilter, InternalApiKeyFilter
    ├── mapper/          # UserMapper.java
    ├── util/            # CookieUtil.java
    └── exception/       # GlobalExceptionHandler, AuthException
```

Every service follows the same layered convention: `controller → service → repository → entity`, with `config`, `security`, `dto`, and `exception` packages, Flyway migrations in `src/main/resources/db/migration`, and its own `Dockerfile`.

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Installation |
|---|---|---|
| Docker & Docker Compose | 24+ | [Download](https://docs.docker.com/get-docker/) |
| Node.js | 20+ | [Download](https://nodejs.org/) |
| Java JDK *(optional, for local Maven runs)* | 21+ | [Download](https://adoptium.net/) |
| Maven *(optional, for local Maven runs)* | 3.9+ | [Download](https://maven.apache.org/download.cgi) |

> The fastest path requires **only Docker** for the backend and **Node.js** for the frontend dev server. PostgreSQL, Redis, and RabbitMQ run as containers — no local installs needed.

### Quick Start

#### 1️⃣ Configure environment variables

```bash
# Clone the repository
git clone [https://github.com/murarivamsharaj/CardPro-ai.git](https://github.com/murarivamsharaj/CardPro-ai.git)
cd cardpro-ai

# Create your .env from the template
cp .env.example .env
```

Edit `.env` and set the secrets your stack needs:

```env
# JWT (shared signing secret — all services)
JWT_SECRET=your-256-bit-secret-key-here-change-in-production

# Internal Auth (inter-service API key)
INTERNAL_API_KEY=change-this-to-a-secure-random-key

# AI Services (used by ai-service)
OPENAI_API_KEY=sk-your-openai-key
REMOVEBG_API_KEY=your-removebg-key
REPLICATE_API_KEY=your-replicate-key

# Payments (used by payment-service)
RAZORPAY_KEY_ID=rzp_live_your_key
RAZORPAY_KEY_SECRET=your_razorpay_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# Eureka dashboard credentials
EUREKA_USERNAME=eureka
EUREKA_PASSWORD=eureka123
```

> Every variable has a sensible default baked into the compose files, so the stack boots even before you set real keys — you'll just get placeholder values for OpenAI/Razorpay until you add your own.

#### 2️⃣ Start the backend with Docker Compose

```bash
# Build and start everything in the background
docker compose up -d

# Watch the services come up
docker compose ps
```

Docker Compose handles the startup order for you (`depends_on` with healthchecks): PostgreSQL & Redis → Eureka → domain services → Gateway → Frontend container.

#### 3️⃣ Run the frontend in development mode

```bash
cd FRONTEND
npm install
npm run dev
```

Open **http://localhost:5173** — the Vite dev server proxies `/api` requests to the API Gateway at `http://localhost:8765`, so the frontend talks to the full backend with zero extra configuration.

---

## 🏃 Running the Backend

### Option 1: Full Stack with Docker Compose (recommended)

One command brings up PostgreSQL, Redis, all 8 core microservices, and the production frontend container:

```bash
docker compose up -d
```

| Component | URL |
|---|---|
| 🖥️ Frontend (container) | http://localhost:3000 |
| 🚪 API Gateway | http://localhost:8765 |
| 🧭 Eureka Dashboard | http://localhost:8761 |
| 🗄️ PostgreSQL | localhost:5432 (`postgres` / `postgres`) |
| ⚡ Redis | localhost:6379 |

```bash
# Follow logs for a specific service
docker compose logs -f auth-service

# Stop the stack
docker compose down
```

### Option 2: Backend Stack with RabbitMQ + Catalog Services

The root compose targets the 8 core services. For the **full backend topology** — including **RabbitMQ**, `user-service`, and `product-service` — use the backend compose file:

```bash
docker compose -f BACKEND/docker-compose.yml up -d --build
```

This additionally exposes:

| Component | URL |
|---|---|
| 🐇 RabbitMQ (AMQP) | localhost:5672 |
| 🐇 RabbitMQ Management UI | http://localhost:15672 (`cardpro` / `cardpro`) |
| 👤 user-service | localhost:8087 |
| 📦 product-service | localhost:8088 |

### Option 3: Run with Maven (local development)

```bash
# 1. Build all services (from BACKEND/)
cd BACKEND
mvn clean package -DskipTests

# 2. Start infrastructure (PostgreSQL + Redis) — either Docker or local installs
docker compose up -d postgres redis   # databases are created by the init script

# 3. Start services in order, each in its own terminal
cd discovery-service && mvn spring-boot:run   # Eureka FIRST (8761)
cd auth-service     && mvn spring-boot:run    # 8081
cd card-service     && mvn spring-boot:run    # 8082
cd lead-service     && mvn spring-boot:run    # 8083
cd ai-service       && mvn spring-boot:run    # 8084
cd payment-service  && mvn spring-boot:run    # 8085
cd order-service    && mvn spring-boot:run    # 8086

# 4. Gateway LAST (8765) — it routes via Eureka and needs the registry populated
cd gateway-service && mvn spring-boot:run
```

Export `JWT_SECRET` and `INTERNAL_API_KEY` in your shell (or set them in your IDE run configuration) before starting the services.

### Service Startup Order

```text
1. discovery-service   (Eureka must be up first)
2. gateway-service     (Gateway needs Eureka)
3. auth-service        (Other services depend on auth)
4. card-service        (Needs auth for token validation)
5. lead-service        (Needs card for profile validation)
6. payment-service     (Needs auth for user validation)
7. order-service       (Needs product catalog via Feign)
8. ai-service          (Stateless, can start anytime)
```

### Verify the Backend is Running

```bash
# Eureka — all services should appear in the registry
curl http://localhost:8761/actuator/health

# Gateway
curl http://localhost:8765/actuator/health

# Test auth through the gateway
curl -X POST http://localhost:8765/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 🖥️ Running the Frontend

The frontend is a **React 18 + TypeScript + Vite 5 + Tailwind CSS 3** single-page application.

```bash
cd FRONTEND

# Install dependencies
npm install

# Start the development server (HMR enabled)
npm run dev
# → http://localhost:5173

# Lint
npm run lint

# Build for production
npm run build

# Preview the production build locally
npm run preview
```

**How the frontend reaches the backend:** the Vite dev server runs on `:5173` and proxies every `/api` request to the Spring Cloud Gateway at `http://localhost:8765` (`vite.config.ts`), so the browser never needs CORS configuration in development. In production, the frontend is served as a static build behind Nginx (port `3000` inside Docker).

---

## 🐳 Docker

### Useful Commands

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Rebuild a specific service
docker compose build auth-service
docker compose up -d auth-service

# Reset everything (including volumes)
docker compose down -v
docker compose up -d
```

### Production Deployment

```bash
# Use production compose file
docker compose -f DOCKER/prod/docker-compose.yml up -d

# With monitoring stack (Prometheus + Grafana)
docker compose -f DOCKER/prod/docker-compose.yml \
               -f DOCKER/monitoring/docker-compose.monitoring.yml up -d
```

### Docker Images

| Service | Image | Size (approx) |
|---|---|---|
| discovery-service | `murarivamsharaj/discovery-service` | 200 MB |
| gateway-service | `murarivamsharaj/gateway-service` | 220 MB |
| auth-service | `murarivamsharaj/auth-service` | 250 MB |
| card-service | `murarivamsharaj/card-service` | 260 MB |
| lead-service | `murarivamsharaj/lead-service` | 240 MB |
| ai-service | `murarivamsharaj/ai-service` | 300 MB |
| payment-service | `murarivamsharaj/payment-service` | 250 MB |
| frontend | `murarivamsharaj/frontend` | 50 MB (Nginx) |

---

## 📚 API Documentation

Complete API documentation is available in the [Postman collection](./POSTMAN/collections/CardPro-AI-API.postman_collection.json).

### API Base URL

| Environment | URL |
|---|---|
| **Local** | `http://localhost:8765/api/v1` |
| **Staging** | `https://staging-api.cardpro.ai/api/v1` |
| **Production** | `https://api.cardpro.ai/api/v1` |

### Authentication APIs

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | Public |
| `POST` | `/auth/login` | Login and get JWT | Public |
| `POST` | `/auth/refresh` | Refresh JWT token | Bearer |

### Card Profile APIs

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/cards/{slug}` | Get public profile (cached) | Public |
| `GET` | `/cards/me` | Get my profile | Bearer |
| `POST` | `/cards` | Create profile | Bearer |
| `PUT` | `/cards/me` | Update profile | Bearer |
| `DELETE` | `/cards/me` | Delete profile | Bearer |

### Lead APIs

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/leads` | Submit a lead | Public |
| `GET` | `/leads` | Get my leads | Bearer |

### AI APIs

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/ai/generate-bio` | Generate AI bio | Bearer |
| `POST` | `/ai/upscale-photo` | AI photo enhancement | Bearer |

### Payment APIs

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/payments/create-order` | Create Razorpay order | Bearer |
| `POST` | `/payments/verify` | Verify payment | Bearer |
| `POST` | `/payments/webhook` | Razorpay webhook | Signature |

### Standard Response Format

```json
{
  "status": "success",
  "data": {},
  "message": "Operation completed",
  "timestamp": "2026-07-27T10:30:00Z"
}
```

---

## 🔮 Future Scope

| Feature | Target Phase |
|---|---|
| 📱 **PWA Support** — Offline viewing, push notifications | Phase 11 |
| 🔑 **OAuth / Social Login** — Google, LinkedIn, GitHub SSO | Phase 12 |
| 👥 **Multi-Profile Support** — Personal + Business profiles | Phase 13 |
| 🏢 **Organization Accounts** — Team management with RBAC | Phase 14 |
| 📈 **Advanced Analytics** — Time-series graphs, geographic heatmaps | Phase 15 |
| 📧 **Email Notifications** — Lead alerts, payment receipts | Phase 15 |
| 🎨 **Custom Template Builder** — Drag-and-drop card designer | Phase 16 |
| 💬 **WhatsApp Business API** — Direct message sending | Phase 17 |
| 💱 **Multi-Currency Support** — Expand beyond INR | Phase 17 |
| 📱 **Native Mobile Apps** — React Native (iOS + Android) | Phase 18 |
| 🗣️ **AI Voice Bio** — Voice recording → AI transcription → Bio | Phase 18 |
| 🖼️ **AI Headshot Generation** — Generate professional photos from prompts | Phase 19 |

---

## 👥 Contributors

- **Chittipoola Murari** — Lead Full Stack Developer & Architect

---

## 🤝 Contributing

Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <hr/>
  <p>
    <sub>Built with ❤️ using Java 21, Spring Boot 3, React 18, and a lot of ☕</sub>
  </p>
  <p>
    <sub>© 2026 CardPro AI. All rights reserved.</sub>
  </p>
</div>