<div align="center">
  <img src="[https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)" alt="Java 21"/>
  <img src="[https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)" alt="Spring Boot 3"/>
  <img src="[https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB](https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)" alt="React 18"/>
  <img src="[https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)" alt="PostgreSQL 15"/>
  <img src="[https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)" alt="Redis 7"/>
  <img src="[https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)" alt="Docker"/>
  <br/>
  <img src="[https://img.shields.io/badge/Microservices-Spring_Cloud-6DB33F?style=flat-square](https://img.shields.io/badge/Microservices-Spring_Cloud-6DB33F?style=flat-square)" alt="Microservices"/>
  <img src="[https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=json-web-tokens](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=json-web-tokens)" alt="JWT"/>
  <img src="[https://img.shields.io/badge/Razorpay-Payments-02042B?style=flat-square&logo=razorpay](https://img.shields.io/badge/Razorpay-Payments-02042B?style=flat-square&logo=razorpay)" alt="Razorpay"/>
  <img src="[https://img.shields.io/badge/OpenAI-GPT-412991?style=flat-square&logo=openai](https://img.shields.io/badge/OpenAI-GPT-412991?style=flat-square&logo=openai)" alt="OpenAI"/>
  <img src="[https://img.shields.io/badge/license-MIT-green?style=flat-square](https://img.shields.io/badge/license-MIT-green?style=flat-square)" alt="MIT License"/>
</div>

<br/>

<div align="center">
  <h1>🚀 CardPro AI</h1>
  <h3>AI-Powered Digital Visiting Card & Lead Generation SaaS Platform</h3>
  <p><em>Replace physical business cards with intelligent, lead-generating digital profiles — powered by AI.</em></p>
</div>

<br/>

---

## 📋 Table of Contents

- [Project Description](#-project-description)
- [Architecture](#-architecture)
- [Features](#-features)
- [Microservices](#-microservices)
- [Technology Stack](#-technology-stack)
- [Folder Structure](#-folder-structure)
- [Getting Started](#-getting-started)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running Backend](#-running-backend)
- [Running Frontend](#-running-frontend)
- [Docker](#-docker)
- [API Documentation](#-api-documentation)
- [Future Scope](#-future-scope)
- [Contributors](#-contributors)
- [License](#-license)

---

## 📖 Project Description

**CardPro AI** is a high-performance **micro-SaaS** platform that revolutionizes professional networking by replacing traditional business cards with intelligent, AI-powered digital profiles that actively generate leads.

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
| [🏗️ Architecture v1.0](./DOCS/CardPro_AI_Microservices_Architecture_v1.0.md) | Microservices architecture design document |
| [📁 Project Structure](./DOCS/CardPro_AI_Complete_Project_Folder_Structure.md) | Complete project folder structure |

---

## 🏗️ Architecture

The platform follows a **microservices architecture** using Spring Cloud, with each service owning its data and scaling independently.

```
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
├────────────────────────────┼────────────────────────────────────────┤
│                    │                   │              │             │
│              ┌─────┴─────┐     ┌───────┴──────┐   ┌───┴───────┐     │
│              │ Auth Svc  │     │  Card Svc    │   │ Lead Svc  │     │
│              │ (8081)    │     │  (8082)      │   │ (8083)    │     │
│              └─────┬─────┘     └───────┬──────┘   └────┬──────┘     │
│                    │                   │               │            │
│              ┌─────┴─────┐     ┌───────┴──────┐        │            │
│              │  AI Svc   │     │ Payment Svc  │        │            │
│              │  (8084)   │     │  (8085)      │        │            │
│              └─────┬─────┘     └───────┬──────┘        │            │
│                    │                   │               │            │
├────────────────────┼───────────────────┼───────────────┼────────────┤
│                    ▼                   ▼               ▼            │
│              ┌──────────────────────────────────────────────┐       │
│              │     PostgreSQL  |  Redis  |  Eureka          │       │
│              └──────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

| Decision | Implementation |
|---|---|
| **Service Discovery** | Eureka Server — all services register dynamically |
| **API Gateway** | Spring Cloud Gateway — single entry point, JWT validation at edge |
| **Authentication** | JWT tokens validated by Gateway; user info propagated via headers |
| **Caching** | Redis cache-aside pattern for public profile reads (90%+ hit ratio) |
| **Inter-Service** | OpenFeign clients with internal API key authentication |
| **Async Events** | Redis Streams for lead capture → AI follow-up pipeline |
| **Database per Service** | Each microservice owns its PostgreSQL schema |

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

## 🧩 Microservices

| Service | Port | Description | Database |
|---|---|---|---|
| 🔍 **discovery-service** | `8761` | Eureka Service Registry — all services register here | None |
| 🚪 **gateway-service** | `8765` | Spring Cloud Gateway — JWT validation, rate limiting, routing | Redis |
| 🔐 **auth-service** | `8081` | User registration, login, JWT token management | `auth_db` |
| 🪪 **card-service** | `8082` | Card profile CRUD, Redis caching, public viewer | `card_db` + Redis |
| 📋 **lead-service** | `8083` | Lead capture, credit management, event publishing | `lead_db` |
| 🤖 **ai-service** | `8084` | OpenAI integration, photo upscaling, follow-up generation | None (stateless) |
| 💳 **payment-service** | `8085` | Razorpay orders, webhook verification, fulfillment | `payment_db` |

### Service Dependency Graph

```
gateway ───▶ auth ───▶ card ───▶ redis
    │                  │
    ├──▶ lead ────────▶ ai (async via Redis Streams)
    │
    └──▶ payment ───▶ card/lead (fulfillment on success)
```

---

## 🛠️ Technology Stack

### Backend

| Technology | Purpose |
|---|---|
| **Java 21** | Core language with records, pattern matching, virtual threads |
| **Spring Boot 3.3** | Production-grade microservices framework |
| **Spring Cloud Gateway** | API Gateway with reactive routing |
| **Spring Security 6** | JWT authentication, role-based authorization |
| **Spring Data JPA** | ORM with Hibernate 6 |
| **PostgreSQL 15** | Primary database with JSONB support |
| **Redis 7** | Caching, rate limiting, token blacklist, async events |
| **Flyway** | Database migrations |
| **OpenFeign** | Declarative HTTP clients for inter-service calls |
| **Resilience4j** | Circuit breaker for external AI API calls |
| **Razorpay SDK** | Payment gateway integration |
| **Lombok** | Boilerplate reduction |
| **Micrometer** | Metrics and monitoring (Prometheus) |

### Frontend

| Technology | Purpose |
|---|---|
| **React 18** | UI framework with hooks and suspense |
| **TypeScript** | Type-safe JavaScript |
| **Vite 5** | Fast build tool and HMR |
| **Tailwind CSS 3** | Utility-first styling |
| **Zustand** | Lightweight state management |
| **React Router 6** | Client-side routing |
| **React Hook Form** | Performant form management |
| **Axios** | HTTP client with interceptors |
| **qrcode.react** | QR code generation |
| **vcard-creator** | vCard (.vcf) file creation |

### DevOps

| Tool | Purpose |
|---|---|
| **Docker & Compose** | Containerization and local orchestration |
| **GitHub Actions** | CI/CD pipelines |
| **Prometheus + Grafana** | Monitoring and alerting |
| **Nginx** | Reverse proxy and SSL termination |

---

## 📁 Folder Structure

```
cardpro-ai/
│
├── .github/                        # CI/CD workflows, issue/PR templates
├── architecture/                   # Architecture documentation
├── BACKEND/                        # All 7 microservices
│   ├── discovery-service/          # Eureka Server
│   ├── gateway-service/            # API Gateway
│   ├── auth-service/               # Authentication
│   ├── card-service/               # Card Profiles
│   ├── lead-service/               # Lead Capture
│   ├── ai-service/                 # AI Processing
│   ├── payment-service/            # Payments
│   └── pom.xml                     # Parent POM
│
├── FRONTEND/                       # React + Vite + TypeScript
│   └── src/
│       ├── components/             # UI components
│       ├── pages/                  # Route pages
│       ├── services/               # API service layer
│       ├── store/                  # Zustand state
│       └── utils/                  # vCard, QR, formatters
│
├── DATABASE/                       # SQL schemas, migrations, seeds
├── DOCKER/                         # Docker configs, Nginx, monitoring
├── DOCS/                           # SRS, Architecture docs
└── POSTMAN/                        # API collections & environments
```

### Microservice Package Structure (Example: auth-service)

```
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

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Installation |
|---|---|---|
| Java JDK | 21+ | [Download](https://adoptium.net/) |
| Maven | 3.9+ | [Download](https://maven.apache.org/download.cgi) |
| Node.js | 20+ | [Download](https://nodejs.org/) |
| Docker | 24+ | [Download](https://docs.docker.com/get-docker/) |
| PostgreSQL | 15+ | [Download](https://www.postgresql.org/download/) |
| Redis | 7+ | [Download](https://redis.io/download/) |

### Quick Start with Docker

```bash
# 1. Clone the repository
git clone https://github.com/murarivamsharaj/CardPro-ai.git
cd cardpro-ai

# 2. Configure environment variables
cp .env.example .env
# Edit .env with your API keys (OpenAI, Razorpay, etc.)

# 3. Start all services
docker compose up -d

# 4. Verify services are running
docker compose ps

# 5. Access the application
# Frontend: http://localhost:3000
# API Gateway: http://localhost:8765
# Eureka Dashboard: http://localhost:8761
```

### Manual Installation

#### 1️⃣ Database Setup

```bash
# Create all databases
createdb auth_db
createdb card_db
createdb lead_db
createdb payment_db
```

#### 2️⃣ Environment Configuration

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```env
# JWT
JWT_SECRET=your-256-bit-secret-key

# Internal Auth
INTERNAL_API_KEY=your-secure-internal-api-key

# AI Services
OPENAI_API_KEY=sk-your-openai-key
REMOVEBG_API_KEY=your-removebg-key
REPLICATE_API_KEY=your-replicate-key

# Payments
RAZORPAY_KEY_ID=rzp_live_your_key
RAZORPAY_KEY_SECRET=your_razorpay_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret
```

---

## 🏃 Running Backend

### Option 1: Run All Services with Maven

```bash
# Build all services
cd BACKEND
mvn clean package -DskipTests

# Start Eureka first, then start other services in separate terminals
# Terminal 1: Discovery Service
cd discovery-service && mvn spring-boot:run

# Terminal 2: Gateway Service
cd gateway-service && mvn spring-boot:run

# Terminal 3-7: Other services (auth, card, lead, ai, payment)
cd auth-service && mvn spring-boot:run
```

### Option 2: Run Individual Service (Development)

```bash
cd BACKEND/auth-service

# With Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with Java directly after building
mvn clean package -DskipTests
java -jar target/auth-service-*.jar
```

### Service Startup Order

```
1. discovery-service   (Eureka must be up first)
2. gateway-service     (Gateway needs Eureka)
3. auth-service        (Other services depend on auth)
4. card-service        (Needs auth for token validation)
5. lead-service        (Needs card for profile validation)
6. payment-service     (Needs auth for user validation)
7. ai-service          (Stateless, can start anytime)
```

### Verify Backend is Running

```bash
# Check Eureka Dashboard
curl http://localhost:8761/actuator/health

# Check Gateway
curl http://localhost:8765/actuator/health

# Test Auth API
curl -X POST http://localhost:8765/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 🖥️ Running Frontend

```bash
cd FRONTEND

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

The frontend will be available at **http://localhost:5173** with hot reload enabled.

---

## 🐳 Docker

### Docker Compose Services

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Rebuild specific service
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

# With monitoring stack
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
| **Staging** | `[https://staging-api.cardpro.ai/api/v1](https://staging-api.cardpro.ai/api/v1)` |
| **Production** | `[https://api.cardpro.ai/api/v1](https://api.cardpro.ai/api/v1)` |

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

- **Senior Software Architect** — System design, architecture, microservices decomposition
- **Chittipoola Murari** — Core Developer

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

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
