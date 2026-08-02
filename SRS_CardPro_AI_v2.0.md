# Software Requirements Specification (SRS)

## CardPro AI — Digital Visiting Card & Lead SaaS

| Document Version | 2.0 (Master Blueprint) |
|---|---|
| Project | CardPro AI |
| Stack | Spring Boot 3 + React 18 + PostgreSQL |
| Date | July 27, 2026 |
| Status | Draft for Development |
| Author | Senior Software Architect |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Purpose](#2-purpose)
3. [Scope](#3-scope)
4. [Objectives](#4-objectives)
5. [Functional Requirements](#5-functional-requirements)
6. [Non-Functional Requirements](#6-non-functional-requirements)
7. [User Roles](#7-user-roles)
8. [Technology Stack](#8-technology-stack)
9. [System Architecture](#9-system-architecture)
10. [Database Overview](#10-database-overview)
11. [API Overview](#11-api-overview)
12. [Future Scope](#12-future-scope)

---

## 1. Introduction

CardPro AI is a high-performance micro-SaaS platform designed to replace physical business cards with intelligent, AI-powered digital profiles that actively generate leads. The platform disrupts the traditional digital visiting card market by offering a **freemium + microtransaction monetization model** — eliminating monthly subscriptions entirely. Service professionals such as doctors, consultants, freelancers, real estate agents, and small business owners can create stunning digital business cards with zero upfront cost and pay only for premium upgrades on a one-time basis.

The system leverages **native AI integrations** as its primary differentiator: AI-generated professional bios from rough notes, AI-powered photo enhancement and background removal, and AI-generated WhatsApp follow-up templates for every captured lead. These features reduce onboarding friction and provide ongoing value that static digital card solutions cannot match.

---

## 2. Purpose

The purpose of this Software Requirements Specification (SRS) document is to provide a comprehensive and unambiguous description of the CardPro AI platform. It serves as the **single source of truth** for:

- **Development Teams** — To understand exactly what must be built across all 10 development phases.
- **Architects** — To validate system design decisions, database schema, API contracts, and integration points.
- **QA & Testing** — To derive test cases, acceptance criteria, and performance benchmarks.
- **Stakeholders** — To align on feature scope, monetization strategy, and timeline expectations.
- **AI Development Assistants** — To serve as the grounding document for AI-assisted code generation throughout the project lifecycle.

---

## 3. Scope

### 3.1 In Scope

| Category | Details |
|---|---|
| **Core Product** | Complete digital visiting card platform with public profile viewing, admin dashboard, and lead management |
| **Authentication** | JWT-based registration, login, and session management |
| **Profile Management** | CRUD operations for card profiles with dynamic JSONB data storage |
| **Public Viewer** | High-performance public profile page with Redis caching, template rendering, and mobile-first design |
| **Lead Capture System** | Two-way lead capture modal, lead database, and admin lead management table |
| **AI Bio Generation** | Integration with OpenAI API to convert rough user notes into professional bios |
| **AI Photo Enhancement** | Vision API integration for background removal and 4K upscaling (Remove.bg / Replicate ESRGAN) |
| **AI Lead Follow-up** | Auto-generated WhatsApp message templates for each captured lead |
| **Analytics** | Page view counting (Redis-backed), link click tracking, and lead capture metrics |
| **Payment Gateway** | Razorpay integration for one-time microtransactions (templates, NFC, lead packs, AI services) |
| **Core Utilities** | QR code generation, vCard (.vcf) file creation, UPI payment QR display |
| **Admin Dashboard** | Split-screen live editor, lead management table, analytics views, store/upgrades section |

### 3.2 Out of Scope

| Item | Rationale |
|---|---|
| **Native Mobile Applications** (iOS/Android) | The platform is mobile-first responsive web; native apps are Phase 11+ |
| **Real-Time Chat / Messaging** | Communication is handled via Click-to-WhatsApp and Contact Form only |
| **Multi-Tenant Organizations** | Current scope is individual professionals; team/enterprise is future scope |
| **Custom Template Builder** | Templates are code-defined React components; no drag-and-drop builder in v1 |
| **Email Marketing Automation** | Lead follow-ups are WhatsApp-based only; email sequences are future scope |
| **Offline Mode / PWA** | Not planned for initial release |
| **SSO / OAuth Social Login** | Email-password JWT auth only in v1 |

---

## 4. Objectives

### 4.1 Business Objectives

| # | Objective | Success Metric |
|---|---|---|
| BO-1 | Maximize user acquisition through zero-cost entry | 10,000+ registered users in first 6 months |
| BO-2 | Generate revenue via one-time microtransactions | 15%+ conversion rate from free to paid |
| BO-3 | Reduce user onboarding friction with AI tools | < 5 minutes to create a publishable card |
| BO-4 | Create viral lead-generation loop for users | 25 free leads per user driving organic adoption |
| BO-5 | Achieve market differentiation through AI features | NPS score > 50 within 3 months of launch |

### 4.2 Technical Objectives

| # | Objective | Success Metric |
|---|---|---|
| TO-1 | Sub-200ms API response time for public profile pages | Lighthouse Performance score > 90 |
| TO-2 | 99.9% uptime for public viewer endpoints | Monitoring dashboard |
| TO-3 | Handle 1000+ concurrent profile views | Load test without degradation |
| TO-4 | Secure payment processing with PCI-DSS alignment | Zero security incidents |
| TO-5 | Fully containerized deployment pipeline | CI/CD automated deploy in < 10 minutes |

---

## 5. Functional Requirements

### 5.1 Authentication Module (FR-AUTH)

| ID | Requirement | Priority |
|---|---|---|
| FR-AUTH-01 | The system shall allow new users to register using email and password | High |
| FR-AUTH-02 | Passwords shall be hashed using Bcrypt before storage | Critical |
| FR-AUTH-03 | The system shall issue JWT tokens upon successful authentication | High |
| FR-AUTH-04 | JWT tokens shall expire after a configurable time period (default: 24 hours) | High |
| FR-AUTH-05 | The system shall validate JWT tokens on all protected API endpoints | Critical |
| FR-AUTH-06 | Users shall be able to log out, invalidating their current session token | Medium |
| FR-AUTH-07 | The login/registration UI shall display clear validation error messages | Medium |

### 5.2 Profile Management Module (FR-PROFILE)

| ID | Requirement | Priority |
|---|---|---|
| FR-PROF-01 | Users shall create a digital card profile with a unique public slug | High |
| FR-PROF-02 | Profile data shall include: Name, Bio, Phone, Email, Skills, Portfolio links, LinkedIn, GitHub, YouTube, Social Media Links, Services, Testimonials, Gallery, Appointment Booking link, Contact Form toggle, and Theme Colors | High |
| FR-PROF-03 | Profile data shall be stored as JSONB in PostgreSQL | High |
| FR-PROF-04 | Users shall select a template_id that maps to a React UI component | Medium |
| FR-PROF-05 | Users shall update their profile in real-time via the Admin Editor | High |
| FR-PROF-06 | Users shall be able to upload a profile image (avatar) | Medium |
| FR-PROF-07 | AI-processed avatar URL (ai_avatar_url) shall be stored separately | Medium |
| FR-PROF-08 | Deleted profiles shall be soft-deleted or permanently removed with confirmation | Low |

### 5.3 Public Viewer Module (FR-VIEWER)

| ID | Requirement | Priority |
|---|---|---|
| FR-VIEW-01 | The public viewer shall render a profile by its unique slug (e.g., /card/johndoe) | Critical |
| FR-VIEW-02 | The page shall be mobile-first (max-width 480px, centered on desktop) | High |
| FR-VIEW-03 | The system shall check Redis cache first before querying PostgreSQL | Critical |
| FR-VIEW-04 | On cache miss, system shall fetch from PostgreSQL, write to Redis, return to client | Critical |
| FR-VIEW-05 | Each profile view shall trigger an async event to increment the view counter | Medium |
| FR-VIEW-06 | The viewer shall display: Profile Header (Image, Name, Bio, Skills tags), Quick Actions Grid (Call, WhatsApp, Email, Map, Appointment), Media & Links (LinkedIn, GitHub, YouTube, Social Media), Professional Info (Services, Portfolio, Testimonials, Gallery), Contact/Lead Capture section, UPI QR section | High |
| FR-VIEW-07 | A sticky footer "Save Contact" button shall trigger vCard download | High |
| FR-VIEW-08 | The page shall achieve Lighthouse performance score > 90 | High |

### 5.4 Lead Capture Module (FR-LEAD)

| ID | Requirement | Priority |
|---|---|---|
| FR-LEAD-01 | A lead capture modal shall appear when a viewer tries to save a contact or access locked information | High |
| FR-LEAD-02 | The modal shall require visitor Name and Phone number | High |
| FR-LEAD-03 | Upon submission, the system shall save the lead linked to the card profile | High |
| FR-LEAD-04 | Users with zero lead credits remaining shall not be able to view new leads | Medium |
| FR-LEAD-05 | The system shall generate an AI-powered WhatsApp follow-up message template for each new lead | Medium |
| FR-LEAD-06 | Admin users shall view all captured leads in a data grid with timestamps | High |
| FR-LEAD-07 | Admin users shall be able to copy/access the AI-generated follow-up text | Medium |
| FR-LEAD-08 | Lead credits shall be refillable via the LEAD_PACK microtransaction (199 INR for 100 credits) | Medium |

### 5.5 Admin Dashboard Module (FR-ADMIN)

| ID | Requirement | Priority |
|---|---|---|
| FR-ADM-01 | The admin dashboard shall be a protected route requiring JWT authentication | Critical |
| FR-ADM-02 | The dashboard layout shall include: Sidebar Navigation (left) + Main Content (center) | High |
| FR-ADM-03 | The Card Editor shall be a split-screen layout | High |
| FR-ADM-04 | Left side (Editor): Form inputs for all profile fields including Personal Info, Skills, Portfolio, Social URLs (LinkedIn, GitHub, YouTube), Services, Testimonials, Gallery uploads, Appointment Booking, Contact Form toggle, Theme colors, and a "Generate AI Bio" button | High |
| FR-ADM-05 | Right side (Preview): Live mobile preview rendering the selected template_id with real-time updates via Zustand state management | High |
| FR-ADM-06 | The Lead Management section shall display a data grid with lead name, phone, timestamp, and "View AI Follow-up" action | High |
| FR-ADM-07 | The Analytics section shall display page views, link clicks, and captured lead counts | Medium |
| FR-ADM-08 | The Store/Upgrades section shall display locked premium templates, NFC card option, and "Buy Now" Razorpay checkout buttons | Medium |

### 5.6 AI Integration Module (FR-AI)

| ID | Requirement | Priority |
|---|---|---|
| FR-AI-01 | The system shall integrate OpenAI API for AI-powered bio generation | High |
| FR-AI-02 | Users shall input rough notes/keywords and receive a polished professional bio | High |
| FR-AI-03 | The AI Photo Upscale feature shall accept a low-quality selfie upload | Medium |
| FR-AI-04 | The backend shall send the image to a Vision API (Remove.bg or Replicate ESRGAN) | Medium |
| FR-AI-05 | The AI shall remove backgrounds, apply a professional solid color, and upscale to 4K | Medium |
| FR-AI-06 | AI Photo Upscale shall require a ₹49 microtransaction payment before processing | Medium |
| FR-AI-07 | The system shall auto-generate a WhatsApp follow-up message when a lead is captured | Medium |
| FR-AI-08 | AI-generated content shall have reasonable fallback content if API calls fail | Low |

### 5.7 Payment Module (FR-PAY)

| ID | Requirement | Priority |
|---|---|---|
| FR-PAY-01 | The system shall integrate Razorpay for payment processing | High |
| FR-PAY-02 | The system shall support the following purchasable items: Premium Templates (₹149), NFC Card (₹999), Custom Domain (₹499), Lead Pack 100 credits (₹199), AI Photo Upscale (₹49) | High |
| FR-PAY-03 | The system shall create a Razorpay Order via backend API before checkout | High |
| FR-PAY-04 | A secure Razorpay Webhook shall verify payment success | Critical |
| FR-PAY-05 | Failed payments shall not grant access to the purchased item | Critical |
| FR-PAY-06 | All transactions shall be recorded in the transactions table with status tracking | High |
| FR-PAY-07 | Delivered items (templates, credits) shall be unlocked immediately upon payment confirmation | High |
| FR-PAY-08 | The system shall display UPI QR codes on public profiles (separate from card payment) | Medium |

### 5.8 Analytics Module (FR-ANALYTICS)

| ID | Requirement | Priority |
|---|---|---|
| FR-ANL-01 | The system shall track page views for each card profile using Redis counters | Medium |
| FR-ANL-02 | The system shall track link clicks on social/profile links | Medium |
| FR-ANL-03 | The system shall track lead capture counts per profile | Medium |
| FR-ANL-04 | Analytics data shall be displayed in the Admin Dashboard | Medium |
| FR-ANL-05 | View counts shall be incremented asynchronously to avoid blocking profile rendering | Medium |

### 5.9 Utility Module (FR-UTIL)

| ID | Requirement | Priority |
|---|---|---|
| FR-UTIL-01 | The system shall generate scannable QR codes for each card profile using qrcode.react | High |
| FR-UTIL-02 | The system shall generate vCard (.vcf) files client-side for saving to device contacts | High |
| FR-UTIL-03 | Click-to-Call and Click-to-WhatsApp links shall use proper tel: and whatsapp: URI schemes | Medium |
| FR-UTIL-04 | Google Maps links shall deep-link to the user's specified location | Medium |
| FR-UTIL-05 | UPI deep-link buttons shall open UPI apps for payment | Low |

---

## 6. Non-Functional Requirements

### 6.1 Performance (NFR-PERF)

| ID | Requirement | Target |
|---|---|---|
| NFR-PERF-01 | Public profile page load time (server-side) | < 200ms P95 |
| NFR-PERF-02 | Lighthouse Performance Score | > 90 |
| NFR-PERF-03 | API response time for cached resources | < 50ms |
| NFR-PERF-04 | Concurrent user support | 1,000+ concurrent profile views |
| NFR-PERF-05 | Admin dashboard initial load time | < 2 seconds |
| NFR-PERF-06 | AI bio generation response time | < 5 seconds |
| NFR-PERF-07 | Image upload and AI processing time | < 10 seconds |

### 6.2 Scalability (NFR-SCALE)

| ID | Requirement | Target |
|---|---|---|
| NFR-SCALE-01 | Horizontal scaling capability for stateless Spring Boot services | Auto-scale via container orchestration |
| NFR-SCALE-02 | Redis caching layer shall absorb read-heavy traffic patterns | 90%+ cache hit rate for public profiles |
| NFR-SCALE-03 | PostgreSQL read replicas for analytics queries | Separate read/write concerns |
| NFR-SCALE-04 | CDN integration for static assets (images, templates) | Global edge delivery |

### 6.3 Security (NFR-SEC)

| ID | Requirement | Target |
|---|---|---|
| NFR-SEC-01 | All passwords hashed with Bcrypt (cost factor >= 10) | Compliance |
| NFR-SEC-02 | JWT tokens signed with strong secret (HS256 or RS256) | Compliance |
| NFR-SEC-03 | All API calls over HTTPS only | Enforcement |
| NFR-SEC-04 | SQL injection prevention via parameterized queries / JPA | Zero vulnerabilities |
| NFR-SEC-05 | XSS prevention via React's built-in sanitization + CSP headers | Zero vulnerabilities |
| NFR-SEC-06 | CSRF protection for state-changing requests | Enabled |
| NFR-SEC-07 | Rate limiting on auth endpoints (login/register) | 5 attempts per minute per IP |
| NFR-SEC-08 | Secure webhook verification for Razorpay callbacks | HMAC signature validation |
| NFR-SEC-09 | Environment secrets managed via environment variables, never committed to code | Strict policy |

### 6.4 Reliability & Availability (NFR-REL)

| ID | Requirement | Target |
|---|---|---|
| NFR-REL-01 | System uptime (public viewer) | 99.9% |
| NFR-REL-02 | Graceful degradation when AI APIs are unavailable | Return fallback/default content |
| NFR-REL-03 | Database backup frequency | Daily automated backups |
| NFR-REL-04 | Recovery Point Objective (RPO) | 24 hours |
| NFR-REL-05 | Recovery Time Objective (RTO) | 4 hours |

### 6.5 Usability (NFR-UX)

| ID | Requirement | Target |
|---|---|---|
| NFR-UX-01 | Mobile-first responsive design for all public pages | Max-width 480px |
| NFR-UX-02 | Admin dashboard accessible on tablet and desktop | Minimum 1024px width |
| NFR-UX-03 | Time to create a publishable card | < 5 minutes |
| NFR-UX-04 | Admin editor changes reflected in live preview | Real-time (< 100ms) |
| NFR-UX-05 | All error messages user-friendly and actionable | No stack traces exposed |
| NFR-UX-06 | Support for dark mode in premium templates | Phase 4 |

### 6.6 Maintainability (NFR-MAINT)

| ID | Requirement | Target |
|---|---|---|
| NFR-MAINT-01 | Modular package structure following domain-driven design | Clear separation of concerns |
| NFR-MAINT-02 | Comprehensive API documentation via Swagger/OpenAPI | All endpoints documented |
| NFR-MAINT-03 | Unit test coverage | > 80% for service/business logic |
| NFR-MAINT-04 | Integration tests for all API endpoints | > 90% coverage |
| NFR-MAINT-05 | CI/CD pipeline with automated testing | Every PR validated |
| NFR-MAINT-06 | Logging standards (structured JSON logs) | All services emit consistent logs |

---

## 7. User Roles

### 7.1 Role Definitions

| Role | Description | Access Level |
|---|---|---|
| **Anonymous Visitor** | A user viewing a public card profile without authentication | Read-only: View public profile, submit lead form |
| **Registered User** | An authenticated user who owns a card profile | Full CRUD on own profile, view leads, make purchases |
| **Admin** | System administrator with platform-wide access | User management, content moderation, transaction oversight |

### 7.2 Role-Permission Matrix

| Feature | Anonymous Visitor | Registered User | Admin |
|---|---|---|---|
| View Public Profile | ✅ | ✅ | ✅ |
| Submit Lead Form | ✅ | N/A | N/A |
| Register/Login | ❌ | ✅ | ✅ |
| Create/Edit Profile | ❌ | ✅ (own) | ✅ (any) |
| View Captured Leads | ❌ | ✅ (own) | ✅ (all) |
| AI Bio Generation | ❌ | ✅ | ✅ |
| AI Photo Upscale | ❌ | ✅ (paid) | ✅ |
| Purchase Items | ❌ | ✅ | ✅ |
| View Analytics | ❌ | ✅ (own) | ✅ (all) |
| Manage Templates | ❌ | ❌ | ✅ |
| Manage Users | ❌ | ❌ | ✅ |
| View All Transactions | ❌ | ❌ | ✅ |

---

## 8. Technology Stack

### 8.1 Backend

| Technology | Version | Purpose | Rationale |
|---|---|---|---|
| **Java** | 17+ | Core language | LTS, modern features, wide ecosystem |
| **Spring Boot** | 3.x | Application framework | Production-grade, embedded servers, auto-configuration |
| **Spring Security** | 6.x | Authentication & authorization | JWT support, method-level security |
| **Spring Data JPA** | 3.x | ORM & database access | Type-safe queries, entity mapping |
| **PostgreSQL** | 15+ | Primary database | JSONB support, reliability, open-source |
| **Redis** | 7+ | Caching & rate limiting | Sub-millisecond reads, counter support |
| **Flyway** | Latest | Database migration | Version-controlled schema changes |
| **OpenAPI / Swagger** | Latest | API documentation | Auto-generated, interactive docs |
| **Lombok** | Latest | Boilerplate reduction | @Data, @Builder, @Slf4j annotations |
| **Docker** | Latest | Containerization | Reproducible builds, easy deployment |

### 8.2 Frontend

| Technology | Version | Purpose | Rationale |
|---|---|---|---|
| **React** | 18.x | UI framework | Component-based, large ecosystem |
| **Vite** | 5+ | Build tool | Fast HMR, optimized builds |
| **React Router** | 6.x | Client-side routing | Nested routes, lazy loading |
| **Zustand** | Latest | State management | Lightweight, real-time state sync |
| **Tailwind CSS** | 3+ | Utility-first styling | Rapid development, responsive design |
| **qrcode.react** | Latest | QR code generation | Client-side rendering, no backend needed |
| **vcard-creator** | Latest | vCard generation | Client-side .vcf file creation |
| **Axios** | Latest | HTTP client | Interceptors, request/response transforms |
| **React Hook Form** | Latest | Form management | Performant, minimal re-renders |

### 8.3 AI & Integrations

| Service | Purpose |
|---|---|
| **OpenAI API (GPT-4 / GPT-3.5)** | AI bio generation |
| **Remove.bg API** | Background removal for profile photos |
| **Replicate ESRGAN** | 4K image upscaling alternative |
| **Razorpay** | Payment gateway for Indian market (Stripe as international alternative) |

### 8.4 DevOps & Infrastructure

| Tool | Purpose |
|---|---|
| **Docker & Docker Compose** | Local development and containerized deployment |
| **GitHub Actions** | CI/CD pipeline |
| **Vercel** | React frontend hosting (edge-optimized) |
| **Render / Azure** | Spring Boot backend hosting |
| **Redis Cloud / Upstash** | Managed Redis service |
| **Cloudflare** | CDN, DNS, DDoS protection |

---

## 9. System Architecture

### 9.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                 │
│                                                                     │
│  ┌──────────────────────┐         ┌──────────────────────────┐      │
│  │   React SPA (Vite)   │         │   Mobile Browser (PWA)   │      │
│  │                      │         │   - Public Viewer         │      │
│  │   - Admin Dashboard  │         │   - Lead Form             │      │
│  │   - Auth Pages       │         │   - vCard Download        │      │
│  │   - Store/Upgrades   │         │   - QR Scan               │      │
│  └──────────┬───────────┘         └─────────────┬────────────┘      │
│             │                                   │                    │
│             └──────────────┬────────────────────┘                    │
│                            │ HTTPS                                  │
└────────────────────────────┼────────────────────────────────────────┘
                             │
┌────────────────────────────┼────────────────────────────────────────┐
│                    API GATEWAY  (Nginx / Cloudflare)                │
│                            │                                        │
│                     /api/v1/*                                       │
│                            │                                        │
├────────────────────────────┼────────────────────────────────────────┤
│                    BACKEND LAYER  (Spring Boot 3)                   │
│                            │                                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │  Auth    │ │ Profile  │ │  Lead    │ │   AI     │ │ Payment  │  │
│  │ Service  │ │ Service  │ │ Service  │ │ Service  │ │ Service  │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
│       │            │            │            │            │         │
│  ┌────┴────────────┴────────────┴────────────┴────────────┴─────┐   │
│  │                    COMMON MODULES                              │   │
│  │  - Security Config (JWT Filter)                               │   │
│  │  - Global Exception Handler                                   │   │
│  │  - Redis Cache Manager                                        │   │
│  │  - Webhook Validator                                          │   │
│  │  - Async Event Publisher                                      │   │
│  └───────────────────────────────────────────────────────────────┘   │
│                            │                                        │
├────────────────────────────┼────────────────────────────────────────┤
│                    DATA LAYER                                       │
│                            │                                        │
│  ┌──────────────────┐ ┌──────────────┐  ┌──────────────────────┐   │
│  │    PostgreSQL     │ │    Redis      │  │   External APIs      │   │
│  │                   │ │               │  │                      │   │
│  │  - users          │ │ - Profile     │  │  - OpenAI (GPT)      │   │
│  │  - card_profiles  │ │   Cache       │  │  - Remove.bg         │   │
│  │  - leads          │ │ - View        │  │  - Replicate         │   │
│  │  - transactions   │ │   Counters    │  │  - Razorpay          │   │
│  │                   │ │ - Rate Limits │  │                      │   │
│  └──────────────────┘ └──────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 9.2 Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| **Application Architecture** | Modular Monolith (Phase 1-10) | Avoid premature microservice complexity. All services within a single Spring Boot deployment, separated by Java packages. Extract to microservices if scaling demands it post-launch. |
| **API Style** | RESTful JSON over HTTPS | Universal compatibility, simple to implement, cacheable |
| **Caching Strategy** | Cache-Aside with Redis | High read volume on public profiles; Redis provides sub-millisecond reads and atomic counters |
| **State Management** | Zustand (React) | Lighter than Redux, sufficient for real-time editor preview, no boilerplate |
| **Database Migrations** | Flyway | Version-controlled, repeatable, integrates with Spring Boot |
| **Image Storage** | Cloud CDN (Cloudinary / AWS S3 + CloudFront) | Offload image processing, global CDN delivery, not in scope for DB |
| **AI Integration** | Synchronous request-response with timeout + fallback | Simple integration; async queue pattern if latency becomes problematic |

### 9.3 Request Flow — Public Profile View

```
1. Browser requests GET /card/{slug}
2. Nginx/Cloudflare reverse proxy → Spring Boot
3. Spring Security filter: PUBLIC route — no auth required
4. ProfileController.getProfile(slug):
   a. Check Redis cache for key "profile:{slug}"
   b. CACHE HIT → Return cached JSON (50ms)
   c. CACHE MISS → Query PostgreSQL for card_profiles WHERE slug = ?
      - Write result to Redis with TTL (e.g., 300 seconds)
      - Return JSON to client
5. Async: Publish ViewEvent to increment view counter in Redis
6. React renders profile using template_id → template component
7. Browser displays: Profile Header, Quick Actions, Media, Services, etc.
```

### 9.4 Request Flow — Lead Capture + AI Follow-up

```
1. Visitor submits Name + Phone on lead modal
2. POST /api/v1/leads { profile_id, visitor_name, visitor_phone }
3. Spring Boot validates and saves lead to PostgreSQL
4. Backend deducts 1 lead credit from profile owner (if applicable)
5. Async: AI Service generates WhatsApp follow-up message via OpenAI
   → Stores in leads.ai_followup column
6. Response: 201 Created with lead confirmation
7. Admin Dashboard: New lead appears in real-time (polling or WebSocket)
```

---

## 10. Database Overview

### 10.1 Entity-Relationship Diagram (Textual)

```
┌─────────────┐       ┌─────────────────┐       ┌─────────────┐
│    users     │ 1───* │  card_profiles   │ 1───* │    leads     │
│─────────────│       │─────────────────│       │─────────────│
│ id (UUID)   │       │ id (UUID)       │       │ id (UUID)   │
│ email       │       │ user_id (FK)    │       │ profile_id  │
│ password_   │       │ slug (UNIQUE)   │       │ visitor_    │
│   hash      │       │ template_id     │       │   name      │
│ lead_       │       │ profile_data    │       │ visitor_    │
│   credits   │       │   (JSONB)       │       │   phone     │
│ created_at  │       │ ai_avatar_url   │       │ ai_followup │
└─────────────┘       └─────────────────┘       │ captured_at │
       │                                         └─────────────┘
       │
       │ 1
       │
       ▼
┌─────────────────────┐
│    transactions      │
│─────────────────────│
│ id (UUID)           │
│ user_id (FK)        │
│ rzp_order_id (UQ)   │
│ amount (DECIMAL)    │
│ item_type (VARCHAR) │
│ status (VARCHAR)    │
│ created_at          │
└─────────────────────┘
```

### 10.2 Complete Table Definitions

#### `users`

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique user identifier |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Authentication email |
| password_hash | VARCHAR(255) | NOT NULL | Bcrypt hashed password |
| lead_credits | INTEGER | NOT NULL, DEFAULT 25 | Credits for viewing captured leads |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Account creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes:** `idx_users_email` on `email`

---

#### `card_profiles`

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique profile identifier |
| user_id | UUID | NOT NULL, FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE | Owner of the profile |
| slug | VARCHAR(100) | UNIQUE, NOT NULL | Public URL slug (e.g., "johndoe") |
| template_id | VARCHAR(50) | NOT NULL, DEFAULT 'basic' | Maps to React UI component |
| profile_data | JSONB | NOT NULL | All profile content: name, bio, phone, links, skills, portfolio, services, testimonials, gallery, theme, etc. |
| ai_avatar_url | VARCHAR(500) | NULLABLE | URL of AI-processed profile image |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Soft delete / visibility toggle |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Profile creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes:**
- `idx_card_profiles_user_id` on `user_id`
- `idx_card_profiles_slug` on `slug` (UNIQUE)
- `idx_card_profiles_template_id` on `template_id`

---

#### `leads`

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Unique lead identifier |
| profile_id | UUID | NOT NULL, FOREIGN KEY REFERENCES card_profiles(id) ON DELETE CASCADE | Profile that captured this lead |
| visitor_name | VARCHAR(150) | NOT NULL | Name submitted by profile viewer |
| visitor_phone | VARCHAR(20) | NOT NULL | Phone number submitted |
| ai_followup | TEXT | NULLABLE | Pre-generated WhatsApp message template |
| captured_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Timestamp of lead capture |

**Indexes:**
- `idx_leads_profile_id` on `profile_id`
- `idx_leads_captured_at` on `captured_at`

---

#### `transactions`

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | UUID | PRIMARY KEY, DEFAULT gen_random_uuid() | Internal transaction ID |
| user_id | UUID | NOT NULL, FOREIGN KEY REFERENCES users(id) | Buyer of the item |
| rzp_order_id | VARCHAR(100) | UNIQUE | Razorpay Order ID |
| rzp_payment_id | VARCHAR(100) | NULLABLE | Razorpay Payment ID (set on success) |
| amount | DECIMAL(10, 2) | NOT NULL | Amount in INR |
| item_type | VARCHAR(50) | NOT NULL | One of: 'TEMPLATE', 'NFC', 'CUSTOM_DOMAIN', 'LEAD_PACK', 'AI_PHOTO' |
| item_details | JSONB | NULLABLE | Additional item-specific metadata |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 'PENDING', 'SUCCESS', 'FAILED' |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Transaction creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Last status update |

**Indexes:**
- `idx_transactions_user_id` on `user_id`
- `idx_transactions_rzp_order_id` on `rzp_order_id` (UNIQUE)
- `idx_transactions_status` on `status`

### 10.3 JSONB Profile Data Schema (profile_data)

```json
{
  "name": "Dr. Sharma",
  "bio": "Cardiologist with 15+ years of experience",
  "phone": "+919876543210",
  "email": "dr.sharma@example.com",
  "skills": ["Cardiology", "Heart Surgery", "Preventive Care"],
  "portfolio": [
    { "title": "Heart Health Guide", "url": "https://..." }
  ],
  "linkedin": "https://linkedin.com/in/drsharma",
  "github": "https://github.com/drsharma",
  "youtube": "https://youtube.com/@drsharma",
  "social_links": [
    { "platform": "Instagram", "url": "https://instagram.com/drsharma" }
  ],
  "services": [
    { "name": "ECG Test", "description": "Detailed heart evaluation", "price": "₹500" }
  ],
  "testimonials": [
    { "name": "Rajesh K.", "text": "Excellent doctor!", "rating": 5 }
  ],
  "gallery": [
    { "url": "https://...clinic-photo.jpg", "caption": "Our Clinic" }
  ],
  "appointment_booking": "https://practo.com/drsharma",
  "contact_form": true,
  "theme": {
    "primary_color": "#1E40AF",
    "secondary_color": "#DBEAFE",
    "font_family": "Inter"
  },
  "upiqr": "upi://pay?pa=drsharma@upi&pn=DrSharma"
}
```

---

## 11. API Overview

### 11.1 Base URL

```
Production:  https://api.cardpro.ai/api/v1
Staging:     https://staging-api.cardpro.ai/api/v1
Local:       http://localhost:8080/api/v1
```

### 11.2 Authentication APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register a new user |
| POST | `/auth/login` | Public | Authenticate and return JWT |
| POST | `/auth/refresh` | Bearer | Refresh an expiring JWT |
| POST | `/auth/logout` | Bearer | Invalidate current token |

### 11.3 Profile APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/profiles/{slug}` | Public | Fetch public profile (Redis-cached) |
| GET | `/profiles/me` | Bearer | Fetch authenticated user's profile |
| POST | `/profiles` | Bearer | Create a new card profile |
| PUT | `/profiles/me` | Bearer | Update authenticated user's profile |
| DELETE | `/profiles/me` | Bearer | Delete profile |
| POST | `/profiles/me/ai-avatar` | Bearer | Upload and trigger AI photo enhancement |

### 11.4 Lead APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/leads` | Public | Submit a lead for a profile |
| GET | `/leads` | Bearer | Get leads for authenticated user's profile |
| GET | `/leads/{id}/followup` | Bearer | Get AI follow-up text for specific lead |

### 11.5 Payment APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/payments/create-order` | Bearer | Create a Razorpay order |
| POST | `/payments/verify` | Bearer | Verify payment signature client-side |
| POST | `/payments/webhook` | Public* | Razorpay webhook (verified via signature) |
| GET | `/payments/history` | Bearer | Get authenticated user's transaction history |

### 11.6 AI APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/ai/generate-bio` | Bearer | Generate professional bio from raw text |
| POST | `/ai/upscale-photo` | Bearer | Process image (requires prior payment) |

### 11.7 Analytics APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/analytics/profile/{profileId}` | Bearer | Get view count, link clicks, lead stats |

### 11.8 Admin APIs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/admin/users` | Admin | List all users |
| GET | `/admin/transactions` | Admin | List all transactions |
| GET | `/admin/profiles` | Admin | List all profiles |
| PUT | `/admin/users/{id}/credits` | Admin | Adjust user lead credits |

> `*` = Webhook endpoint is publicly accessible but validated via Razorpay HMAC signature, not JWT.

### 11.9 Standard Response Format

```json
// Success
{
  "status": "success",
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2026-07-27T10:30:00Z"
}

// Error
{
  "status": "error",
  "error": {
    "code": "PROFILE_NOT_FOUND",
    "message": "No profile found with the provided slug",
    "details": {}
  },
  "timestamp": "2026-07-27T10:30:00Z"
}

// Paginated
{
  "status": "success",
  "data": [ ... ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 150,
    "total_pages": 8
  },
  "timestamp": "2026-07-27T10:30:00Z"
}
```

### 11.10 HTTP Status Codes

| Code | Usage |
|---|---|
| 200 | Successful GET, PUT, PATCH |
| 201 | Successful POST (resource created) |
| 204 | Successful DELETE |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid JWT) |
| 403 | Forbidden (insufficient role) |
| 404 | Resource not found |
| 409 | Conflict (duplicate slug, email) |
| 429 | Rate limit exceeded |
| 500 | Internal Server Error |

---

## 12. Future Scope

The following features are explicitly deferred to post-v1 releases but should be architected for in the initial design where feasible:

| Feature | Description | Target Phase |
|---|---|---|
| **PWA Support** | Offline profile viewing, installable to home screen, push notifications for new leads | Phase 11 |
| **OAuth / Social Login** | Google, LinkedIn, and GitHub single sign-on | Phase 12 |
| **Multi-Profile Support** | Allow a single user to manage multiple card profiles (e.g., personal + business) | Phase 13 |
| **Team / Organization Accounts** | Multiple users under one organization with role-based access | Phase 14 |
| **Analytics Deep Dive** | Time-series graphs, geographic heatmaps, device/browser analytics, export to CSV | Phase 15 |
| **Email Notifications** | Automated email alerts for new leads, payment receipts, and credit depletion | Phase 15 |
| **Custom Template Builder** | Drag-and-drop editor for non-technical users to create custom card layouts | Phase 16 |
| **SMS Lead Alerts** | Real-time SMS notification when a new lead is captured | Phase 16 |
| **WhatsApp Business API** | Direct WhatsApp message sending for lead follow-ups (not just template generation) | Phase 17 |
| **Multi-Currency Support** | Expand payments beyond INR for international users | Phase 17 |
| **Native Mobile Apps** | React Native or Flutter for iOS + Android with NFC tap-to-share | Phase 18 |
| **AI Voice Bio** | Record audio → AI transcription → bio generation | Phase 18 |
| **AI Profile Photo Generation** | Generate complete professional headshots from text prompts | Phase 19 |
| **Blockchain Verification** | Tamper-proof credential / certification display on profiles | Phase 20 |

---

## Appendix A: Glossary

| Term | Definition |
|---|---|
| **Micro-SaaS** | A small, focused SaaS product built for a specific niche market |
| **Freemium** | Free basic service with paid premium features |
| **Microtransaction** | Small, one-time payment for a specific item or feature |
| **JWT** | JSON Web Token — stateless authentication mechanism |
| **JSONB** | PostgreSQL binary JSON data type with indexing support |
| **Slug** | URL-friendly unique identifier (e.g., /card/johndoe) |
| **vCard / .vcf** | Standard file format for electronic business cards |
| **UPI** | Unified Payments Interface — Indian real-time payment system |
| **NFC** | Near Field Communication — tap-to-share technology for smart cards |
| **Lead Credit** | Consumable unit that allows a user to view a captured lead's details |
| **Razorpay** | Indian payment gateway supporting credit/debit cards, UPI, net banking, and wallets |
| **ESRGAN** | Enhanced Super-Resolution Generative Adversarial Network — AI image upscaling model |

## Appendix B: Document Revision History

| Version | Date | Author | Changes |
|---|---|---|---|
| 1.0 | July 15, 2026 | Senior Architect | Initial draft based on product requirements |
| 2.0 | July 27, 2026 | Senior Architect | Complete SRS with all sections, API contracts, database schema, architecture diagrams, NFRs, and future scope |

---

> **End of Document — CardPro AI SRS v2.0**
>
> *This document serves as the single source of truth for all development phases.
> All AI-assisted code generation should reference this SRS for context and requirements.*

