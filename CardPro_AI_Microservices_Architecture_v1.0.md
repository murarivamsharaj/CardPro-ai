# CardPro AI — Microservices Architecture Design

## Production-Grade Distributed System Blueprint

| Document Version | 1.0 |
|---|---|
| Project | CardPro AI |
| Architecture Pattern | Microservices (Spring Cloud) |
| Java Version | 21 |
| Spring Boot Version | 3.x |
| Date | July 27, 2026 |
| Author | Senior Java Spring Boot Microservices Architect |

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture Diagram](#2-architecture-diagram)
3. [Service Definitions](#3-service-definitions)
   - [3.1 discovery-service](#31-discovery-service)
   - [3.2 gateway-service](#32-gateway-service)
   - [3.3 auth-service](#33-auth-service)
   - [3.4 card-service](#34-card-service)
   - [3.5 lead-service](#35-lead-service)
   - [3.6 ai-service](#36-ai-service)
   - [3.7 payment-service](#37-payment-service)
4. [Inter-Service Communication](#4-inter-service-communication)
5. [Complete Request Flows](#5-complete-request-flows)
6. [Data Architecture](#6-data-architecture)
7. [Containerization & Deployment](#7-containerization--deployment)
8. [Environment Variable Matrix](#8-environment-variable-matrix)
9. [Security Architecture](#9-security-architecture)
10. [Project Directory Structure](#10-project-directory-structure)

---

## 1. System Overview

CardPro AI is decomposed into **7 microservices** following Domain-Driven Design (DDD) principles. Each service owns its data, scales independently, and communicates via REST over HTTP (synchronous) and Redis Pub/Sub / RabbitMQ (asynchronous) for eventual consistency.

### Core Architectural Principles

| Principle | Implementation |
|---|---|
| **Database per Service** | Each service owns its schema/table(s) — no cross-service DB queries |
| **API Gateway as Entry Point** | All external traffic routes through Spring Cloud Gateway |
| **Service Discovery** | Eureka for dynamic service registration and lookup |
| **JWT Token Propagation** | Gateway validates JWT, passes userId/roles in headers to downstream services |
| **Redis Caching** | Centralized Redis cluster for caching (card-profiles) and rate limiting |
| **Async Eventual Consistency** | Redis Pub/Streams for view count increments, async AI follow-up generation |
| **Centralized Configuration** | Spring Cloud Config Server (or environment variables per container) |

### Service Overview Map

```
                           ┌──────────────────────────────┐
                           │        React Frontend         │
                           │     (Vercel / S3 + CDN)       │
                           └──────────────┬───────────────┘
                                          │ HTTPS
                                          ▼
                           ┌──────────────────────────────┐
                           │      Spring Cloud Gateway     │
                           │    gateway-service (8765)     │
                           │   ┌─ JWT Validation Filter ─┐ │
                           │   └─ Route Configuration   ─┘ │
                           └──────┬───────┬───────┬───────┘
                                  │       │       │
                    ┌─────────────┘       │       └─────────────┐
                    │                     │                     │
                    ▼                     ▼                     ▼
           ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
           │   Auth Service  │  │   Card Service │  │   Lead Service  │
           │  (auth-svc)     │  │  (card-svc)    │  │  (lead-svc)     │
           │  Port: 8081     │  │  Port: 8082    │  │  Port: 8083     │
           │  DB: users      │  │  DB: profiles  │  │  DB: leads      │
           └───────┬─────────┘  └───────┬────────┘  └───────┬────────┘
                   │                    │                    │
                   │                    │                    │
                   ▼                    ▼                    ▼
           ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
           │   AI Service    │  │ Payment Service│  │  Redis Cache   │
           │  (ai-svc)       │  │ (pay-svc)     │  │  (Shared)      │
           │  Port: 8084     │  │ Port: 8085    │  │  Port: 6379    │
           │  No local DB    │  │ DB: txs       │  │                │
           └────────────────┘  └────────────────┘  └────────────────┘
                                   │
                                   ▼
                          ┌────────────────┐
                          │   Eureka       │
                          │ Discovery Svc  │
                          │  Port: 8761    │
                          └────────────────┘
```

---

## 2. Architecture Diagram

### 2.1 Complete System Topology

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                               EXTERNAL LAYER                                        │
│                                                                                     │
│  ┌─────────────────────────────┐        ┌────────────────────────────────────┐     │
│  │   Browser (React SPA)       │        │   Razorpay / Stripe               │     │
│  │   - Public Card Viewer      │        │   - Payment Gateway               │     │
│  │   - Admin Dashboard         │        │   - Webhook Callbacks             │     │
│  │   - Auth Pages              │        └──────────────┬─────────────────────┘     │
│  └──────────────┬──────────────┘                       │                           │
│                 │ HTTPS                                │ HTTP POST                 │
│                 ▼                                      │ (Webhook)                 │
│  ┌────────────────────────────────────────────────────────────────────────────┐     │
│  │                    CLOUD FLARE / NGINX REVERSE PROXY                       │     │
│  │              SSL Termination, DDoS Protection, CDN                         │     │
│  └────────────────────────────────┬───────────────────────────────────────────┘     │
│                                   │                                                  │
└───────────────────────────────────┼──────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                     SPRING CLOUD GATEWAY (gateway-service:8765)                      │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │                          GLOBAL FILTERS                                      │   │
│  │  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────────┐   │   │
│  │  │  JWT Auth Filter    │  │  Rate Limiting      │  │  Correlation ID   │   │   │
│  │  │  Validates JWT      │  │  Redis-backed       │  │  Trace Header     │   │   │
│  │  │  Extracts userId    │  │  Per-IP / Per-Token │  │  Injection        │   │   │
│  │  └─────────────────────┘  └─────────────────────┘  └────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────┐   │
│  │                          ROUTE TABLE                                         │   │
│  │  ┌──────────────────┬─────────────────────┬────────────────────────────┐    │   │
│  │  │ Path Pattern      │ Target Service      │ Auth Required             │    │   │
│  │  ├──────────────────┼─────────────────────┼────────────────────────────┤    │   │
│  │  │ /api/v1/auth/**   │ auth-service        │ Public (with rate limit)  │    │   │
│  │  │ /api/v1/cards/**  │ card-service        │ Mixed (GET /{slug}=pub)   │    │   │
│  │  │ /api/v1/leads/**  │ lead-service        │ Mixed (POST=pub, GET=priv) │    │   │
│  │  │ /api/v1/ai/**     │ ai-service          │ Bearer Token Required     │    │   │
│  │  │ /api/v1/payments/**│ payment-service    │ Mixed (webhook=pub)       │    │   │
│  │  │ /api/v1/admin/**  │ card-service        │ ADMIN Role Required       │    │   │
│  │  └──────────────────┴─────────────────────┴────────────────────────────┘    │   │
│  └──────────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────┬───────────────────────────────────────────────────────┘
                               │ Load Balances via Eureka
                               │
          ┌────────────────────┼────────────────────────────────────┐
          │                    │                                     │
          ▼                    ▼                                     ▼
┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────────────┐
│   EUREKA SERVER     │ │   CONFIG SERVER     │ │   Distributed Tracing       │
│   discovery-service  │ │   (Optional)        │ │   (Micrometer + Zipkin)     │
│   Port: 8761         │ │   Port: 8888        │ │                             │
│   All services       │ │   Git-backed config │ │   Centralized logging       │
│   register here      │ │   per service       │ │   (ELK / Loki)              │
└─────────────────────┘ └─────────────────────┘ └─────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────────────────────┐
          │                    │                                     │
          ▼                    ▼                                     ▼
┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────────────┐
│   auth-service       │ │   card-service      │ │   lead-service              │
│   Port: 8081         │ │   Port: 8082        │ │   Port: 8083                │
│   ─────────────────  │ │   ───────────────── │ │   ───────────────────       │
│   DB: PostgreSQL     │ │   DB: PostgreSQL    │ │   DB: PostgreSQL            │
│   Schema: auth_db    │ │   Schema: card_db   │ │   Schema: lead_db           │
│   Tables: users      │ │   Tables:           │ │   Tables: leads             │
│                      │ │   card_profiles     │ │                              │
│                      │ │                     │ │   Redis: Read profile cache │
│                      │ │   Redis: Cache      │ │   (via card-service)        │
│                      │ │   profile:{slug}    │ │                              │
└─────────────────────┘ └─────────────────────┘ └─────────────────────────────┘
          │
          │                    ┌─────────────────────┐ ┌─────────────────────────────┐
          │                    │   ai-service         │ │   payment-service           │
          │                    │   Port: 8084         │ │   Port: 8085                │
          │                    │   ─────────────────  │ │   ───────────────────       │
          │                    │   No Local DB        │ │   DB: PostgreSQL            │
          │                    │   Calls:             │ │   Schema: payment_db        │
          │                    │   - OpenAI API       │ │   Tables: transactions      │
          │                    │   - Remove.bg API    │ │                              │
          │                    │   - Replicate API    │ │   Calls:                    │
          │                    │                      │ │   - Razorpay API            │
          │                    │   Consumes:          │ │   - auth-service (validate) │
          │                    │   - lead-service     │ │   - card-service (unlock)   │
          │                    │   (lead:created)     │ │                              │
          │                    └─────────────────────┘ └─────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              SHARED INFRASTRUCTURE                                   │
│                                                                                     │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────────────────┐  │
│  │   PostgreSQL 15+    │  │   Redis 7+          │  │   RabbitMQ / Redis Streams │  │
│  │                     │  │                     │  │                            │  │
│  │   - auth_db         │  │   - Profile Cache   │  │   - lead:created events    │  │
│  │   - card_db         │  │   - View Counters   │  │   - view:incremented       │  │
│  │   - lead_db         │  │   - Rate Limiting   │  │   - payment:completed      │  │
│  │   - payment_db      │  │   - JWT Blacklist   │  │                            │  │
│  │                     │  │                     │  │                            │  │
│  └─────────────────────┘  └─────────────────────┘  └────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Service Dependency Graph

```
┌──────────────┐         ┌──────────────┐
│  gateway-svc  │ ──────▶│  auth-svc    │
│  (8765)       │         │  (8081)      │
└──────┬───────┘         └──────────────┘
       │                        ▲
       │                        │ JWT validation
       │                        │ (inter-service)
       ▼                        │
┌──────────────┐         ┌──────┴───────┐
│  card-svc    │ ──────▶│  auth-svc    │
│  (8082)      │         │  (8081)      │
└──────┬───────┘         └──────────────┘
       │
       │ Redis Cache
       ▼
┌──────────────┐         ┌──────────────┐
│  lead-svc    │ ──────▶│  card-svc    │
│  (8083)      │         │  (verify     │
└──────┬───────┘         │   profile)   │
       │                 └──────────────┘
       │ async event
       ▼
┌──────────────┐         ┌──────────────┐
│  ai-svc      │ ◀──────│  lead-svc    │
│  (8084)      │         │  (event)     │
└──────────────┘         └──────────────┘
       │
       ▼
┌──────────────┐         ┌──────────────┐
│  payment-svc  │ ──────▶│  auth-svc    │
│  (8085)       │         │  (validate   │
└──────┬───────┘         │   user)      │
       │                 └──────────────┘
       │ on success
       ▼
┌──────────────┐
│  card-svc    │ (unlock template / add credits)
│  lead-svc    │ (refill lead credits)
└──────────────┘
```

---

## 3. Service Definitions

---

### 3.1 discovery-service

#### Purpose

Service Registry for all microservices using **Spring Cloud Netflix Eureka Server**. Every microservice registers itself with Eureka on startup and sends heartbeats to maintain its presence. The Gateway and inter-service clients discover service locations dynamically via Eureka rather than hardcoding IP addresses.

#### Responsibilities

- Maintain registry of all running service instances
- Handle service registration and heartbeat monitoring
- Provide service discovery endpoints to clients
- Remove instances that fail to send heartbeats (30s lease expiration)
- Serve as the source of truth for load-balanced routing

#### Database Tables Used

**None.** Eureka is stateless and does not require a database. It operates purely in-memory.

#### REST APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/eureka/apps` | List all registered applications (XML/JSON) |
| GET | `/eureka/apps/{appName}` | Get instances of a specific application |
| GET | `/eureka/status` | Eureka server health status |

> **Note:** These endpoints are typically consumed internally by Spring Cloud clients, not exposed externally.

#### Package Structure

```
discovery-service/
├── src/main/java/com/cardpro/discovery/
│   ├── DiscoveryServiceApplication.java
│   └── config/
│       └── SecurityConfig.java          # Disable CSRF for Eureka dashboard
├── src/main/resources/
│   ├── application.yml
│   └── bootstrap.yml
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>discovery-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro Discovery Service</name>
    <description>Eureka Service Registry for CardPro AI</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Eureka Server -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>

        <!-- Actuator for health checks -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Security for Eureka Dashboard -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-service

  security:
    user:
      name: ${EUREKA_USERNAME:eureka}
      password: ${EUREKA_PASSWORD:eureka123}

eureka:
  instance:
    hostname: discovery-service
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 5000
    renewal-percent-threshold: 0.85

  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@${eureka.instance.hostname}:${server.port}/eureka/

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `EUREKA_USERNAME` | `eureka` | Basic auth username for Eureka dashboard |
| `EUREKA_PASSWORD` | `eureka123` | Basic auth password for Eureka dashboard |
| `SERVER_PORT` | `8761` | Eureka server port |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| All services | ← Clients register | HTTP (REST) | Register, heartbeat, and discover |
| gateway-service | ← Routes via LB | HTTP (REST) | Gateway uses Eureka for dynamic routing |

---

### 3.2 gateway-service

#### Purpose

**Spring Cloud Gateway** acts as the single entry point for all external traffic. It handles JWT validation at the edge, rate limiting, request/response transformation, and routes requests to the appropriate downstream microservice based on path patterns. It is the **only** service exposed to the public internet.

#### Responsibilities

- **Route all API traffic** — Map `/api/v1/auth/**` → auth-service, `/api/v1/cards/**` → card-service, etc.
- **JWT Validation** — Validate JWT tokens for protected routes; extract `userId`, `email`, `roles` and inject as headers to downstream services
- **Rate Limiting** — Redis-backed `RequestRateLimiter` per IP address for public endpoints (auth, public profile view)
- **CORS Configuration** — Allow requests from React frontend domains
- **Correlation ID Injection** — Generate and propagate `X-Correlation-Id` for distributed tracing
- **Webhook Route** — Allow Razorpay webhook traffic without JWT validation (signature-based auth only)
- **Service Discovery Integration** — Use Eureka for dynamic load-balanced routing

#### Database Tables Used

**None.** The Gateway is stateless and routes traffic only.

#### REST APIs

The Gateway does not define its own APIs. It proxies to downstream services:

| Route Pattern | Target Service | Strip Prefix | Auth |
|---|---|---|---|
| `/api/v1/auth/**` | `lb://auth-service` | `/api/v1` | Public (rate limited) |
| `/api/v1/cards/**` | `lb://card-service` | `/api/v1` | Mixed |
| `/api/v1/leads/**` | `lb://lead-service` | `/api/v1` | Mixed |
| `/api/v1/ai/**` | `lb://ai-service` | `/api/v1` | Bearer JWT |
| `/api/v1/payments/**` | `lb://payment-service` | `/api/v1` | Mixed |
| `/api/v1/admin/**` | `lb://card-service` | `/api/v1` | ADMIN Role |

#### Package Structure

```
gateway-service/
├── src/main/java/com/cardpro/gateway/
│   ├── GatewayServiceApplication.java
│   ├── config/
│   │   ├── RouteConfig.java                  # Bean-based route definitions
│   │   ├── CorsConfig.java                   # CORS configuration
│   │   └── RateLimiterConfig.java            # Redis rate limiter config
│   ├── filter/
│   │   ├── JwtAuthGlobalFilter.java          # JWT validation at edge
│   │   └── CorrelationIdFilter.java          # Trace ID injection
│   └── util/
│       └── JwtUtil.java                      # JWT parsing / validation logic
├── src/main/resources/
│   ├── application.yml
│   └── bootstrap.yml
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>gateway-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro Gateway Service</name>
    <description>Spring Cloud Gateway for CardPro AI</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <!-- Eureka Client for service discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- Redis for rate limiting -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>

        <!-- JWT Library (shared with auth-service) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Micrometer Tracing -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8765

spring:
  application:
    name: gateway-service

  cloud:
    gateway:
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
        - name: RequestRateLimiter
          args:
            redis-rate-limiter:
              replenishRate: 10
              burstCapacity: 20
              requestedTokens: 1
        - name: CorrelationIdFilter

      routes:
        # ── Auth Routes ──
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=1

        # ── Card Profile Routes ──
        - id: card-service
          uri: lb://card-service
          predicates:
            - Path=/api/v1/cards/**
          filters:
            - StripPrefix=1

        # ── Lead Routes ──
        - id: lead-service
          uri: lb://lead-service
          predicates:
            - Path=/api/v1/leads/**
          filters:
            - StripPrefix=1

        # ── AI Routes ──
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/v1/ai/**
          filters:
            - StripPrefix=1
            - name: JwtAuthFilter
              args:
                requiredRoles: USER,ADMIN

        # ── Payment Routes ──
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/v1/payments/**
          filters:
            - StripPrefix=1

        # ── Admin Routes ──
        - id: admin-service
          uri: lb://card-service
          predicates:
            - Path=/api/v1/admin/**
          filters:
            - StripPrefix=1
            - name: JwtAuthFilter
              args:
                requiredRoles: ADMIN

      # Allow webhook traffic without JWT (validated by signature in payment-service)
      routes:
        - id: payment-webhook
          uri: lb://payment-service
          predicates:
            - Path=/api/v1/payments/webhook
          filters:
            - StripPrefix=1

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@discovery-service:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000  # 24 hours

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway
  endpoint:
    health:
      show-details: always
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8765` | Gateway port |
| `JWT_SECRET` | (required) | HMAC-SHA256 key for JWT validation |
| `REDIS_HOST` | `localhost` | Redis host for rate limiting |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |
| `EUREKA_USERNAME` | `eureka` | Eureka dashboard username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka dashboard password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| auth-service | → Route | HTTP | Proxies auth requests |
| card-service | → Route | HTTP | Proxies card requests |
| lead-service | → Route | HTTP | Proxies lead requests |
| ai-service | → Route | HTTP | Proxies AI requests |
| payment-service | → Route | HTTP | Proxies payment requests |
| Eureka | → Discover | HTTP | Discovers service locations |

---

### 3.3 auth-service

#### Purpose

Central authentication and authorization service for CardPro AI. Manages user registration, login, JWT token issuance and refresh, and provides an internal API for other services to validate tokens and retrieve user information.

#### Responsibilities

- **User Registration** — Validate email (uniqueness), hash password with Bcrypt, persist to `users` table
- **User Login** — Authenticate credentials, issue JWT access token + refresh token
- **Token Refresh** — Accept valid refresh token and issue a new access token
- **Token Blacklisting** — Maintain Redis set of invalidated JWTs (logout)
- **Internal Token Validation** — Provide `/internal/validate` endpoint for other services to verify JWT validity
- **User Profile Retrieval** — Provide user info (email, roles, lead credits) to other services via internal API
- **Account Management** — Update email, change password (authenticated)

#### Database Tables Used

| Table | Schema | Purpose |
|---|---|---|
| `users` | `auth_db` | Store user credentials, roles, lead_credits |

**Only `auth-service` reads/writes the `users` table.** All other services call auth-service's internal API to get user data.

#### REST APIs

##### Public Endpoints

| Method | Endpoint | Description | Rate Limit |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user | 5/min per IP |
| POST | `/api/v1/auth/login` | Authenticate and return JWT | 10/min per IP |
| POST | `/api/v1/auth/refresh` | Refresh an expiring JWT | 5/min per IP |
| POST | `/api/v1/auth/logout` | Invalidate current token (add to blacklist) | 10/min per IP |

##### Internal Endpoints (called by Gateway / other services)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/v1/auth/internal/validate` | Validate JWT, return userId + roles + email | Internal API Key |
| GET | `/api/v1/auth/internal/users/{userId}` | Get user details by ID | Internal API Key |
| GET | `/api/v1/auth/internal/users/by-email` | Get user by email | Internal API Key |

#### Package Structure

```
auth-service/
├── src/main/java/com/cardpro/auth/
│   ├── AuthServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java               # Spring Security, password encoder, CORS
│   │   └── RedisConfig.java                  # Redis config for token blacklist
│   ├── controller/
│   │   ├── AuthController.java               # /api/v1/auth/* endpoints
│   │   └── InternalAuthController.java       # /api/v1/auth/internal/* endpoints
│   ├── dto/
│   │   ├── request/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── RefreshTokenRequest.java
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── UserResponse.java
│   │       └── ValidationResponse.java
│   ├── entity/
│   │   └── User.java                        # JPA Entity
│   ├── repository/
│   │   └── UserRepository.java
│   ├── service/
│   │   ├── AuthService.java                 # Registration, login, token logic
│   │   ├── JwtService.java                  # JWT creation, validation, parsing
│   │   └── TokenBlacklistService.java       # Redis-based token invalidation
│   ├── security/
│   │   └── InternalApiKeyFilter.java        # Authenticate inter-service calls
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── AuthException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                        # Flyway migrations
│       └── V1__create_users_table.sql
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>auth-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro Auth Service</name>
    <description>Authentication and Authorization Service</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service

  datasource:
    url: jdbc:postgresql://${AUTH_DB_HOST:localhost}:${AUTH_DB_PORT:5432}/${AUTH_DB_NAME:auth_db}
    username: ${AUTH_DB_USERNAME:postgres}
    password: ${AUTH_DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate   # Flyway manages schema
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@discovery-service:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000           # 24 hours
    refresh-expiration-ms: 2592000000  # 30 days
  internal:
    api-key: ${INTERNAL_API_KEY}        # Shared secret for inter-service calls

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8081` | Service port |
| `AUTH_DB_HOST` | `localhost` | PostgreSQL host for auth_db |
| `AUTH_DB_PORT` | `5432` | PostgreSQL port |
| `AUTH_DB_NAME` | `auth_db` | Database name |
| `AUTH_DB_USERNAME` | `postgres` | Database username |
| `AUTH_DB_PASSWORD` | `postgres` | Database password |
| `REDIS_HOST` | `localhost` | Redis host (for token blacklist) |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |
| `JWT_SECRET` | (required) | 256+ bit secret for signing JWTs |
| `INTERNAL_API_KEY` | (required) | Shared secret for inter-service auth |
| `EUREKA_USERNAME` | `eureka` | Eureka auth username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka auth password |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| gateway-service | ← Receives traffic | HTTP | Routes auth requests to this service |
| card-service | ← Validates tokens | HTTP (int) | Card service calls to validate JWT |
| lead-service | ← Validates tokens | HTTP (int) | Lead service calls to validate JWT |
| payment-service | ← Validates tokens | HTTP (int) | Payment service calls to validate JWT |
| Redis | ← Read/Write | Redis | Store token blacklist |
| Eureka | → Register | HTTP | Register with discovery |

---

### 3.4 card-service

#### Purpose

Core service for managing digital card profiles. This is the most traffic-intensive service — it serves public profile views (read-heavy) and provides CRUD operations for authenticated users to manage their profiles.

#### Responsibilities

- **Profile CRUD** — Create, read, update, delete card profiles with JSONB data
- **Public Profile Retrieval** — Fetch profile by slug with Redis cache-aside pattern
- **Slug Management** — Ensure unique slugs, validate format
- **Profile Caching** — Read from Redis cache first; write to cache on miss
- **View Counter** — Increment view counters in Redis asynchronously
- **Admin Operations** — List all profiles, manage user profiles (ADMIN role)
- **Internal Profile Validation** — Provide `/internal/profiles/{id}` for other services
- **Template Management** — List available templates, unlock premium templates on payment

#### Database Tables Used

| Table | Schema | Purpose |
|---|---|---|
| `card_profiles` | `card_db` | Store all card profile data and metadata |

#### REST APIs

##### Public Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/cards/{slug}` | Public | Fetch public profile (Redis-cached) |

##### Authenticated Endpoints (User)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/cards/me` | Bearer | Get authenticated user's profile |
| POST | `/api/v1/cards` | Bearer | Create a new card profile |
| PUT | `/api/v1/cards/me` | Bearer | Update authenticated user's profile |
| DELETE | `/api/v1/cards/me` | Bearer | Delete profile (soft-delete) |
| POST | `/api/v1/cards/me/avatar` | Bearer | Upload profile avatar image |

##### Admin Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/admin/profiles` | ADMIN | List all profiles (paginated) |
| PUT | `/api/v1/admin/profiles/{id}` | ADMIN | Update any profile |
| DELETE | `/api/v1/admin/profiles/{id}` | ADMIN | Delete any profile |

##### Public Template Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/templates` | Public | List available templates |

##### Internal Endpoints (Inter-service)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/cards/internal/{profileId}` | Internal API Key | Get profile by ID for other services |
| POST | `/api/v1/cards/internal/{profileId}/unlock-template` | Internal API Key | Unlock premium template (called by payment-service) |
| POST | `/api/v1/cards/internal/{profileId}/increment-view` | Internal API Key | Increment view count |

#### Package Structure

```
card-service/
├── src/main/java/com/cardpro/card/
│   ├── CardServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java               # JWT filter, role-based auth
│   │   ├── RedisCacheConfig.java             # Redis template for caching
│   │   └── AsyncConfig.java                  # Async executor for view counters
│   ├── controller/
│   │   ├── CardController.java               # Public and authenticated card endpoints
│   │   ├── AdminCardController.java          # Admin-only card operations
│   │   ├── TemplateController.java           # Template listing
│   │   └── InternalCardController.java       # Inter-service endpoints
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateCardRequest.java
│   │   │   └── UpdateCardRequest.java
│   │   └── response/
│   │       ├── CardResponse.java
│   │       ├── PublicCardResponse.java
│   │       └── TemplateResponse.java
│   ├── entity/
│   │   └── CardProfile.java                 # JPA Entity with JSONB column
│   ├── repository/
│   │   └── CardProfileRepository.java
│   ├── service/
│   │   ├── CardService.java                 # Core business logic
│   │   ├── CardCacheService.java            # Redis cache-aside logic
│   │   ├── SlugService.java                 # Unique slug generation & validation
│   │   ├── ViewCounterService.java          # Async view counting
│   │   └── TemplateService.java             # Template management
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java     # Extract user from JWT
│   │   └── InternalApiKeyFilter.java
│   ├── event/
│   │   └── ViewEventPublisher.java          # Publish async view events
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── CardException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__create_card_profiles_table.sql
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>card-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro Card Service</name>
    <description>Card Profile Management Service</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- OpenFeign for inter-service HTTP calls -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Hibernate Types (JSONB support) -->
        <dependency>
            <groupId>com.vladmihalcea</groupId>
            <artifactId>hibernate-types-60</artifactId>
            <version>2.21.1</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Micrometer for metrics -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: card-service

  datasource:
    url: jdbc:postgresql://${CARD_DB_HOST:localhost}:${CARD_DB_PORT:5432}/${CARD_DB_NAME:card_db}
    username: ${CARD_DB_USERNAME:postgres}
    password: ${CARD_DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20                # Higher pool for read-heavy traffic
      minimum-idle: 10
      read-only: false
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@discovery-service:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET}
  internal:
    api-key: ${INTERNAL_API_KEY}
  cache:
    profile-ttl-seconds: 300               # 5 minutes cache TTL for public profiles

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8082` | Service port |
| `CARD_DB_HOST` | `localhost` | PostgreSQL host for card_db |
| `CARD_DB_PORT` | `5432` | PostgreSQL port |
| `CARD_DB_NAME` | `card_db` | Database name |
| `CARD_DB_USERNAME` | `postgres` | Database username |
| `CARD_DB_PASSWORD` | `postgres` | Database password |
| `REDIS_HOST` | `localhost` | Redis host (caching + view counters) |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |
| `JWT_SECRET` | (required) | JWT secret key (for validation) |
| `INTERNAL_API_KEY` | (required) | Shared secret for inter-service auth |
| `EUREKA_USERNAME` | `eureka` | Eureka username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka password |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| gateway-service | ← Receives traffic | HTTP | Routes card requests to this service |
| auth-service | → Validates JWT | HTTP (Feign) | Validates tokens for authenticated requests |
| lead-service | → Provides profile data | HTTP (int) | Lead service verifies profile exists |
| payment-service | → Unlock template | HTTP (int) | Called after payment success to unlock templates |
| Redis | ← Read/Write | Redis | Cache profiles + view counters |
| Eureka | → Register | HTTP | Register with discovery |

---

### 3.5 lead-service

#### Purpose

Manages the complete lead lifecycle: capture visitor leads from public card profiles, enforce lead credit limits, store lead data, and trigger AI follow-up generation asynchronously.

#### Responsibilities

- **Lead Capture** — Accept visitor name + phone from public profile forms
- **Credit Enforcement** — Check profile owner's remaining lead credits before returning lead details
- **Lead Storage** — Persist leads in PostgreSQL with profile association
- **Lead Retrieval** — Return captured leads to profile owners (authenticated)
- **AI Follow-up Trigger** — Publish `lead:created` event for ai-service to generate WhatsApp template
- **Lead Credit Management** — Deduct credits on lead view, refill on payment

#### Database Tables Used

| Table | Schema | Purpose |
|---|---|---|
| `leads` | `lead_db` | Store captured leads with visitor info and AI follow-up |

#### REST APIs

##### Public Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/leads` | Public | Submit a lead (name + phone) for a profile |

##### Authenticated Endpoints (User)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/leads` | Bearer | Get leads for authenticated user's profile (paginated) |
| GET | `/api/v1/leads/{id}` | Bearer | Get specific lead details |
| GET | `/api/v1/leads/{id}/followup` | Bearer | Get AI follow-up text for a specific lead |

##### Admin Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/admin/leads` | ADMIN | List all leads across all profiles |

##### Internal Endpoints (Inter-service)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/leads/internal/credits/deduct` | Internal API Key | Deduct lead credits (called by card-service) |
| POST | `/api/v1/leads/internal/credits/add` | Internal API Key | Add lead credits (called by payment-service) |

#### Package Structure

```
lead-service/
├── src/main/java/com/cardpro/lead/
│   ├── LeadServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java           # JWT + role-based auth
│   │   └── AsyncConfig.java              # Async event publishing
│   ├── controller/
│   │   ├── LeadController.java           # Public + user lead endpoints
│   │   ├── AdminLeadController.java      # Admin endpoints
│   │   └── InternalLeadController.java   # Inter-service endpoints
│   ├── dto/
│   │   ├── request/
│   │   │   ├── SubmitLeadRequest.java
│   │   │   └── CreditAdjustRequest.java
│   │   └── response/
│   │       ├── LeadResponse.java
│   │       └── LeadFollowupResponse.java
│   ├── entity/
│   │   └── Lead.java                    # JPA Entity
│   ├── repository/
│   │   └── LeadRepository.java
│   ├── service/
│   │   ├── LeadService.java             # Core lead logic
│   │   ├── LeadCreditService.java       # Credit validation and management
│   │   └── LeadEventPublisher.java      # Publish lead:created events
│   ├── client/
│   │   ├── CardServiceClient.java       # Feign client to card-service
│   │   └── AuthServiceClient.java       # Feign client to auth-service
│   ├── security/
│   │   └── InternalApiKeyFilter.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── LeadException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__create_leads_table.sql
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>lead-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro Lead Service</name>
    <description>Lead Capture and Management Service</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- OpenFeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8083

spring:
  application:
    name: lead-service

  datasource:
    url: jdbc:postgresql://${LEAD_DB_HOST:localhost}:${LEAD_DB_PORT:5432}/${LEAD_DB_NAME:lead_db}
    username: ${LEAD_DB_USERNAME:postgres}
    password: ${LEAD_DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@discovery-service:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET}
  internal:
    api-key: ${INTERNAL_API_KEY}

# Async event configuration for lead:created events
lead:
  events:
    enabled: true
    ai-followup-enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8083` | Service port |
| `LEAD_DB_HOST` | `localhost` | PostgreSQL host for lead_db |
| `LEAD_DB_PORT` | `5432` | PostgreSQL port |
| `LEAD_DB_NAME` | `lead_db` | Database name |
| `LEAD_DB_USERNAME` | `postgres` | Database username |
| `LEAD_DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | (required) | JWT secret key |
| `INTERNAL_API_KEY` | (required) | Shared secret for inter-service auth |
| `EUREKA_USERNAME` | `eureka` | Eureka username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka password |
| `LEAD_EVENTS_ENABLED` | `true` | Enable async event publishing |
| `LEAD_AI_FOLLOWUP_ENABLED` | `true` | Enable AI follow-up generation |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| gateway-service | ← Receives traffic | HTTP | Routes lead requests |
| card-service | → Get profile info | HTTP (Feign) | Verify profile exists before lead capture |
| auth-service | → Get user info | HTTP (Feign) | Get lead credits info |
| ai-service | → Publish event | Redis Streams | Trigger AI follow-up generation |
| payment-service | ← Add credits | HTTP (int) | Refill lead credits after payment |
| Eureka | → Register | HTTP | Register with discovery |

---

### 3.6 ai-service

#### Purpose

Central AI processing service. Abstracts all third-party AI API integrations (OpenAI, Remove.bg, Replicate) behind a unified REST API. This service is isolated so AI API latency or failures do not impact core card/lead operations.

#### Responsibilities

- **AI Bio Generation** — Accept raw notes/keywords → call OpenAI GPT → return polished bio
- **AI Photo Upscale** — Accept image upload → call Remove.bg for background removal → call ESRGAN for 4K upscaling → return processed image URL
- **AI Lead Follow-up** — Consume `lead:created` events → call OpenAI → generate WhatsApp message template → store in lead record via lead-service API
- **Payment Verification** — Check with payment-service that the user has paid for AI Photo Upscale before processing
- **Graceful Degradation** — Return fallback/default content when external AI APIs are unreachable

#### Database Tables Used

**None.** This service is stateless. It stores no data locally — it processes requests and writes results back to the calling service's database via internal API calls.

#### REST APIs

##### Authenticated Endpoints (User)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/ai/generate-bio` | Bearer | Generate professional bio from rough notes |
| POST | `/api/v1/ai/upscale-photo` | Bearer | Upload and AI-process profile photo (requires prior payment) |

##### Internal Endpoints (Inter-service / Async Consumers)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/ai/internal/lead-followup` | Internal API Key | Generate follow-up for a specific lead (called by async consumer) |

#### Package Structure

```
ai-service/
├── src/main/java/com/cardpro/ai/
│   ├── AiServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java               # JWT + internal key auth
│   │   ├── OpenAiConfig.java                 # OpenAI client config (Retry, Timeout)
│   │   ├── VisionApiConfig.java              # Remove.bg / Replicate config
│   │   └── AsyncConfig.java                  # Lead follow-up consumer thread pool
│   ├── controller/
│   │   ├── AiController.java                 # /api/v1/ai/* user endpoints
│   │   └── InternalAiController.java         # Inter-service endpoints
│   ├── dto/
│   │   ├── request/
│   │   │   ├── BioGenerationRequest.java
│   │   │   ├── PhotoUpscaleRequest.java
│   │   │   └── LeadFollowupRequest.java
│   │   └── response/
│   │       ├── BioGenerationResponse.java
│   │       ├── PhotoUpscaleResponse.java
│   │       └── LeadFollowupResponse.java
│   ├── service/
│   │   ├── BioGenerationService.java         # OpenAI bio generation
│   │   ├── PhotoEnhancementService.java      # Image processing pipeline
│   │   ├── LeadFollowupService.java          # WhatsApp template generation
│   │   └── AiFallbackService.java            # Fallback content when APIs fail
│   ├── client/
│   │   ├── OpenAiClient.java                 # HTTP client to OpenAI API
│   │   ├── RemoveBgClient.java               # HTTP client to Remove.bg
│   │   ├── ReplicateClient.java              # HTTP client to Replicate
│   │   ├── PaymentServiceClient.java         # Feign client to payment-service
│   │   ├── LeadServiceClient.java            # Feign client to lead-service
│   │   └── AuthServiceClient.java            # Feign client to auth-service
│   ├── consumer/
│   │   └── LeadCreatedConsumer.java          # Async consumer of lead:created events
│   ├── security/
│   │   └── InternalApiKeyFilter.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── AiServiceException.java
│       └── AiApiException.java               # External API failure wrapper
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>ai-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro AI Service</name>
    <description>AI Processing Service for Bio, Photo, and Lead Follow-up</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>  <!-- For non-blocking HTTP calls to AI APIs -->
        </dependency>

        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- OpenFeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- Redis (for consuming events) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Resilience4j for circuit breaker on external APIs -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Micrometer for AI API latency metrics -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8084

spring:
  application:
    name: ai-service

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@discovery-service:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET}
  internal:
    api-key: ${INTERNAL_API_KEY}

  ai:
    # OpenAI Configuration
    openai:
      api-key: ${OPENAI_API_KEY}
      model: ${OPENAI_MODEL:gpt-4}
      bio-generation:
        max-tokens: 500
        temperature: 0.7
        system-prompt: "You are a professional bio writer. Convert the user's notes into a polished, professional biography suitable for a digital business card. Keep it concise and impactful."
        timeout-seconds: 30
      lead-followup:
        max-tokens: 300
        temperature: 0.5
        system-prompt: "Generate a professional WhatsApp follow-up message template for a new lead. Use placeholders like {visitor_name} and {profile_owner_name}."

    # Vision API (Photo Enhancement)
    vision:
      removebg:
        api-key: ${REMOVEBG_API_KEY}
        timeout-seconds: 30
      replicate:
        api-key: ${REPLICATE_API_KEY}
        model: "nightmareai/real-esrgan:42fed1c5"
        timeout-seconds: 60

    # Circuit Breaker Configuration
    circuit-breaker:
      bio-generation:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30000
        sliding-window-size: 10
      photo-enhancement:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60000
        sliding-window-size: 5

    # Fallback responses when AI is unavailable
    fallback:
      bio: "Professional with expertise in [Your Field]. Dedicated to delivering quality service and building lasting relationships."
      lead-followup: "Hi {visitor_name}! Thank you for your interest. I'd love to connect and discuss how I can help. Please feel free to reach out."

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8084` | Service port |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |
| `JWT_SECRET` | (required) | JWT secret key |
| `INTERNAL_API_KEY` | (required) | Shared secret for inter-service auth |
| `OPENAI_API_KEY` | (required) | OpenAI API key |
| `OPENAI_MODEL` | `gpt-4` | OpenAI model to use |
| `REMOVEBG_API_KEY` | (required) | Remove.bg API key |
| `REPLICATE_API_KEY` | (required) | Replicate API key |
| `EUREKA_USERNAME` | `eureka` | Eureka username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka password |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| gateway-service | ← Receives traffic | HTTP | Routes AI requests |
| lead-service | ← Get lead details | HTTP (Feign) | Update lead with AI follow-up text |
| payment-service | ← Verify payment | HTTP (Feign) | Check if user paid for photo upscale |
| auth-service | ← Validate user | HTTP (Feign) | Verify user identity |
| OpenAI | → External | HTTPS | Bio generation + lead follow-up |
| Remove.bg | → External | HTTPS | Background removal |
| Replicate | → External | HTTPS | 4K image upscaling |
| Redis | ← Consume events | Redis Streams | Consume lead:created events |
| Eureka | → Register | HTTP | Register with discovery |

---

### 3.7 payment-service

#### Purpose

Handles all financial transactions for CardPro AI. Integrates with Razorpay (primary) and Stripe (future) for payment processing, verifies webhooks securely, and manages the transaction lifecycle from order creation to fulfillment.

#### Responsibilities

- **Order Creation** — Create Razorpay orders for purchasable items (templates, NFC, lead packs, AI photo)
- **Payment Verification** — Verify Razorpay payment signatures from client-side callback
- **Webhook Handling** — Receive and validate Razorpay webhook events (HMAC-SHA256 signature verification)
- **Transaction Recording** — Persist all transactions with status tracking (PENDING → SUCCESS/FAILED)
- **Fulfillment** — On successful payment, call the appropriate service to unlock/deliver the purchased item
- **Transaction History** — Return authenticated user's purchase history
- **Admin Oversight** — Provide transaction list and refund capability (ADMIN)

#### Database Tables Used

| Table | Schema | Purpose |
|---|---|---|
| `transactions` | `payment_db` | Store all payment transactions with status |

#### REST APIs

##### Public Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/payments/webhook` | Signature | Razorpay webhook (validated by HMAC, not JWT) |

##### Authenticated Endpoints (User)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/payments/create-order` | Bearer | Create a new Razorpay order |
| POST | `/api/v1/payments/verify` | Bearer | Verify payment after client-side success |
| GET | `/api/v1/payments/history` | Bearer | Get authenticated user's transaction history |

##### Admin Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/admin/payments` | ADMIN | List all transactions |
| POST | `/api/v1/admin/payments/{id}/refund` | ADMIN | Process a refund |

##### Internal Endpoints (Inter-service)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/payments/internal/verify/{userId}/{itemType}` | Internal API Key | Check if user has paid for an item (used by ai-service) |

#### Package Structure

```
payment-service/
├── src/main/java/com/cardpro/payment/
│   ├── PaymentServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java               # JWT + internal key auth
│   │   ├── RazorpayConfig.java               # Razorpay client config
│   │   └── WebhookConfig.java                # Webhook signature verification
│   ├── controller/
│   │   ├── PaymentController.java            # /api/v1/payments/* user endpoints
│   │   ├── WebhookController.java            # /api/v1/payments/webhook
│   │   ├── AdminPaymentController.java       # Admin endpoints
│   │   └── InternalPaymentController.java    # Inter-service endpoints
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── VerifyPaymentRequest.java
│   │   │   └── RefundRequest.java
│   │   └── response/
│   │       ├── CreateOrderResponse.java
│   │       ├── VerifyPaymentResponse.java
│   │       └── TransactionResponse.java
│   ├── entity/
│   │   └── Transaction.java                 # JPA Entity
│   ├── repository/
│   │   └── TransactionRepository.java
│   ├── service/
│   │   ├── PaymentService.java               # Core payment orchestration
│   │   ├── RazorpayClientService.java        # Razorpay API integration
│   │   ├── WebhookVerificationService.java   # HMAC signature validation
│   │   └── FulfillmentService.java           # Post-payment delivery to other services
│   ├── client/
│   │   ├── CardServiceClient.java            # Feign: unlock template / update credits
│   │   ├── LeadServiceClient.java            # Feign: add lead credits
│   │   └── AuthServiceClient.java            # Feign: validate user
│   ├── security/
│   │   └── InternalApiKeyFilter.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── PaymentException.java
│       └── WebhookVerificationException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__create_transactions_table.sql
├── pom.xml
└── Dockerfile
```

#### Maven Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.cardpro</groupId>
    <artifactId>payment-service</artifactId>
    <version>1.0.0</version>
    <name>CardPro Payment Service</name>
    <description>Payment Processing and Microtransaction Service</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
        <razorpay-sdk.version>1.4.4</razorpay-sdk.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- OpenFeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- Razorpay SDK -->
        <dependency>
            <groupId>com.razorpay</groupId>
            <artifactId>razorpay-java</artifactId>
            <version>${razorpay-sdk.version}</version>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### application.yml

```yaml
server:
  port: 8085

spring:
  application:
    name: payment-service

  datasource:
    url: jdbc:postgresql://${PAYMENT_DB_HOST:localhost}:${PAYMENT_DB_PORT:5432}/${PAYMENT_DB_NAME:payment_db}
    username: ${PAYMENT_DB_USERNAME:postgres}
    password: ${PAYMENT_DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME:eureka}:${EUREKA_PASSWORD:eureka123}@discovery-service:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET}
  internal:
    api-key: ${INTERNAL_API_KEY}

  razorpay:
    key-id: ${RAZORPAY_KEY_ID}
    key-secret: ${RAZORPAY_KEY_SECRET}
    webhook-secret: ${RAZORPAY_WEBHOOK_SECRET}
    currency: INR

  # Product pricing (in paise for Razorpay)
  products:
    template-premium:
      name: "Premium Template"
      price-paise: 14900    # ₹149
      type: TEMPLATE
    nfc-card:
      name: "NFC Smart Card"
      price-paise: 99900    # ₹999
      type: NFC
    custom-domain:
      name: "Custom Domain"
      price-paise: 49900    # ₹499
      type: CUSTOM_DOMAIN
    lead-pack-100:
      name: "Lead Pack (100 Credits)"
      price-paise: 19900    # ₹199
      type: LEAD_PACK
      credits: 100
    ai-photo-upscale:
      name: "AI Photo Upscale"
      price-paise: 4900     # ₹49
      type: AI_PHOTO

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8085` | Service port |
| `PAYMENT_DB_HOST` | `localhost` | PostgreSQL host for payment_db |
| `PAYMENT_DB_PORT` | `5432` | PostgreSQL port |
| `PAYMENT_DB_NAME` | `payment_db` | Database name |
| `PAYMENT_DB_USERNAME` | `postgres` | Database username |
| `PAYMENT_DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | (required) | JWT secret key |
| `INTERNAL_API_KEY` | (required) | Shared secret for inter-service auth |
| `RAZORPAY_KEY_ID` | (required) | Razorpay API Key ID |
| `RAZORPAY_KEY_SECRET` | (required) | Razorpay API Key Secret |
| `RAZORPAY_WEBHOOK_SECRET` | (required) | Razorpay webhook secret for HMAC verification |
| `EUREKA_USERNAME` | `eureka` | Eureka username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka password |

#### Communication with Other Services

| Service | Direction | Protocol | Purpose |
|---|---|---|---|
| gateway-service | ← Receives traffic | HTTP | Routes payment requests |
| card-service | → Fulfillment | HTTP (Feign) | Unlock premium templates on payment success |
| lead-service | → Fulfillment | HTTP (Feign) | Add lead credits on payment success |
| auth-service | → Validate user | HTTP (Feign) | Verify user exists and get user ID |
| ai-service | ← Payment check | HTTP (int) | ai-service checks if user paid for photo upscale |
| Razorpay | → External | HTTPS | Create orders, payment processing |
| Eureka | → Register | HTTP | Register with discovery |

---

## 4. Inter-Service Communication

### 4.1 Communication Matrix

```
┌─────────────────┬──────────────┬──────────────┬──────────────┬──────────────┬──────────────┬──────────────┐
│  FROM \ TO      │ auth-service │ card-service │ lead-service │  ai-service  │payment-service│  Redis       │
├─────────────────┼──────────────┼──────────────┼──────────────┼──────────────┼──────────────┼──────────────┤
│ gateway-service  │ HTTP (Route) │ HTTP (Route) │ HTTP (Route) │ HTTP (Route) │ HTTP (Route) │ Redis (Rate) │
│ auth-service     │      -      │      -       │      -       │      -       │      -       │ Redis (Black)│
│ card-service     │ HTTP (Feign) │      -      │      -       │      -       │      -       │ Redis (Cache)│
│ lead-service     │ HTTP (Feign) │ HTTP (Feign) │     -        │ Redis Stream │ HTTP (Feign) │      -       │
│ ai-service       │ HTTP (Feign) │      -       │ HTTP (Feign) │     -        │ HTTP (Feign) │ Redis (Cons) │
│ payment-service  │ HTTP (Feign) │ HTTP (Feign) │ HTTP (Feign) │     -        │      -       │      -       │
└─────────────────┴──────────────┴──────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
```

**Legend:**
- `HTTP (Route)` = Traffic routed via API Gateway
- `HTTP (Feign)` = Direct inter-service HTTP call via OpenFeign + Eureka
- `Redis (Cache)` = Read/Write cache
- `Redis (Black)` = Token blacklist
- `Redis (Rate)` = Rate limiting
- `Redis (Stream)` = Event streaming via Redis Streams
- `Redis (Cons)` = Consumer of Redis Streams

### 4.2 Asynchronous Events (Redis Streams)

| Event | Publisher | Consumer(s) | Payload | Trigger |
|---|---|---|---|---|
| `lead:created` | lead-service | ai-service | `{ leadId, profileId, visitorName }` | New lead captured |
| `view:incremented` | card-service | card-service (self) | `{ profileId, slug }` | Public profile viewed |

### 4.3 Inter-Service Feign Client Example

```java
// CardServiceClient.java (used by lead-service to verify profile existence)
@FeignClient(name = "card-service", path = "/api/v1/cards/internal")
public interface CardServiceClient {

    @GetMapping("/{profileId}")
    CardResponse getProfileById(
        @PathVariable UUID profileId,
        @RequestHeader("X-Internal-API-Key") String apiKey
    );
}

// AuthServiceClient.java (used by multiple services to validate tokens)
@FeignClient(name = "auth-service", path = "/api/v1/auth/internal")
public interface AuthServiceClient {

    @GetMapping("/validate")
    ValidationResponse validateToken(
        @RequestHeader("Authorization") String token,
        @RequestHeader("X-Internal-API-Key") String apiKey
    );
}
```

---

## 5. Complete Request Flows

### 5.1 User Login Flow

```
React App                    Gateway                   auth-service               Redis                PostgreSQL
    │                          │                          │                       │                     │
    │  POST /api/v1/auth/login  │                          │                       │                     │
    │  {email, password}        │                          │                       │                     │
    │─────────────────────────►│                          │                       │                     │
    │                          │  Rate Limit Check ──────►│                       │                     │
    │                          │◄─────────────────────────│                       │                     │
    │                          │                          │                       │                     │
    │                          │  POST /api/v1/auth/login  │                       │                     │
    │                          │  {email, password}        │                       │                     │
    │                          │─────────────────────────►│                       │                     │
    │                          │                          │  SELECT * FROM users  │                     │
    │                          │                          │  WHERE email=?        │                     │
    │                          │                          │──────────────────────►│                     │
    │                          │                          │◄──────────────────────│                     │
    │                          │                          │  User found           │                     │
    │                          │                          │                       │                     │
    │                          │                          │  Bcrypt.verify()      │                     │
    │                          │                          │  JWT.create()          │                     │
    │                          │                          │                       │                     │
    │                          │◄─────────────────────────│                       │                     │
    │                          │  200 OK { token, user }  │                       │                     │
    │◄─────────────────────────│                          │                       │                     │
    │                          │                          │                       │                     │
    │  Store JWT in memory     │                          │                       │                     │
    │  (Zustand store)         │                          │                       │                     │
    │  Redirect to /dashboard  │                          │                       │                     │
```

### 5.2 Public Card Profile View Flow

```
Browser (React)            Gateway                  card-service                Redis             PostgreSQL
    │                          │                          │                       │                     │
    │  GET /api/v1/cards/drsharma                         │                       │                     │
    │─────────────────────────►│                          │                       │                     │
    │                          │  Rate Limit Check        │                       │                     │
    │                          │  (Passes - public route)  │                       │                     │
    │                          │                          │                       │                     │
    │                          │  GET /cards/drsharma     │                       │                     │
    │                          │─────────────────────────►│                       │                     │
    │                          │                          │  Redis: GET           │                     │
    │                          │                          │  "profile:drsharma"   │                     │
    │                          │                          │──────────────────────►│                     │
    │                          │                          │◄──────────────────────│                     │
    │                          │                          │  CACHE MISS           │                     │
    │                          │                          │                       │                     │
    │                          │                          │  SELECT * FROM        │                     │
    │                          │                          │  card_profiles        │                     │
    │                          │                          │  WHERE slug='drsharma'│                     │
    │                          │                          │──────────────────────────►                     │
    │                          │                          │◄───────────────────────────                     │
    │                          │                          │  Profile data         │                     │
    │                          │                          │                       │                     │
    │                          │                          │  Redis: SETEX         │                     │
    │                          │                          │  "profile:drsharma"   │                     │
    │                          │                          │   TTL: 300s           │                     │
    │                          │                          │──────────────────────►│                     │
    │                          │                          │◄──────────────────────│                     │
    │                          │                          │                       │                     │
    │                          │  Async: Increment view   │                       │                     │
    │                          │  counter in Redis        │                       │                     │
    │                          │─────────────────────────►│                       │                     │
    │                          │                          │  Redis: INCR          │                     │
    │                          │                          │  "views:drsharma"     │                     │
    │                          │                          │──────────────────────►│                     │
    │                          │                          │◄──────────────────────│                     │
    │                          │                          │                       │                     │
    │                          │◄─────────────────────────│                       │                     │
    │                          │  200 OK { profile }      │                       │                     │
    │◄─────────────────────────│                          │                       │                     │
    │                          │                          │                       │                     │
    │  Render template_id      │                          │                       │                     │
    │  Map to React component  │                          │                       │                     │
    │  Display mobile-first UI │                          │                       │                     │
```

### 5.3 Lead Capture + AI Follow-up Flow

```
Browser (React)          Gateway              lead-service            card-service        auth-service     Redis/AI     PostgreSQL
    │                       │                       │                       │                 │              │             │
    │ POST /api/v1/leads    │                       │                       │                 │              │             │
    │ {profile_id, name,   │                       │                       │                 │              │             │
    │  phone}               │                       │                       │                 │              │             │
    │──────────────────────►│                       │                       │                 │              │             │
    │                       │ POST /leads           │                       │                 │              │             │
    │                       │──────────────────────►│                       │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │                       │                       │ Feign: Verify profile  │                 │              │             │
    │                       │                       │──────────────────────►│                 │              │             │
    │                       │                       │◄──────────────────────│                 │              │             │
    │                       │                       │ Profile valid         │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │                       │                       │ Feign: Get user       │                 │              │             │
    │                       │                       │ credits               │                 │              │             │
    │                       │                       │──────────────────────────────────────►│              │             │
    │                       │                       │◄──────────────────────────────────────│              │             │
    │                       │                       │ User has credits      │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │                       │                       │ INSERT INTO leads     │                 │              │             │
    │                       │                       │─────────────────────────────────────────────────────►             │
    │                       │                       │◄──────────────────────────────────────────────────────             │
    │                       │                       │                       │                 │              │             │
    │                       │                       │ Async: Publish event  │                 │              │             │
    │                       │                       │ "lead:created"        │                 │              │             │
    │                       │                       │─────────────────────────────────────────────────────►             │
    │                       │                       │                       │                 │              │             │
    │                       │◄──────────────────────│                       │                 │              │             │
    │◄──────────────────────│                       │                       │                 │              │             │
    │ 201 Created           │                       │                       │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │ ── ASYNC ──           │                       │                       │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │                       │              ai-service consumes "lead:created"               │              │             │
    │                       │                       │                       │                 │              │             │
    │                       │                       │◄──────────────────────│                 │              │             │
    │                       │                       │ Event consumed        │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │              ai-service                        │                       │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │              POST /leads/{id}/followup         │                       │                 │              │             │
    │              (Internal)──────────────────────►│                       │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │              OpenAI API Call                   │                       │                 │              │             │
    │              "Generate WhatsApp template..."   │                       │                 │              │             │
    │                       │                       │                       │                 │              │             │
    │                       │                       │ UPDATE leads SET      │                 │              │             │
    │                       │                       │ ai_followup=...       │                 │              │             │
    │                       │                       │─────────────────────────────────────────────────────►             │
    │                       │                       │◄──────────────────────────────────────────────────────             │
    │                       │                       │                       │                 │              │             │
```

### 5.4 Payment + Fulfillment Flow

```
React App                    Gateway              payment-service          card-service        Razorpay       PostgreSQL
    │                          │                       │                       │                 │               │
    │ POST /payments/create-order                      │                       │                 │               │
    │ {item_type: TEMPLATE}    │                       │                       │                 │               │
    │─────────────────────────►│                       │                       │                 │               │
    │                          │──────────────────────►│                       │                 │               │
    │                          │                       │                       │                 │               │
    │                          │                       │  INSERT INTO txs      │                 │               │
    │                          │                       │  (status=PENDING)     │                 │               │
    │                          │                       │──────────────────────────────────────────────────────►│
    │                          │                       │◄──────────────────────────────────────────────────────│
    │                          │                       │                       │                 │               │
    │                          │                       │  RazorpayClient       │                 │               │
    │                          │                       │  .createOrder()       │                 │               │
    │                          │                       │───────────────────────────────────────►│               │
    │                          │                       │◄───────────────────────────────────────│               │
    │                          │                       │  { order_id, amount } │                 │               │
    │                          │                       │                       │                 │               │
    │                          │◄──────────────────────│                       │                 │               │
    │                          │  { order_id, key }    │                       │                 │               │
    │◄─────────────────────────│                       │                       │                 │               │
    │                          │                       │                       │                 │               │
    │  Razorpay Checkout       │                       │                       │                 │               │
    │  Opens in browser        │                       │                       │                 │               │
    │  User completes payment  │                       │                       │                 │               │
    │                          │                       │                       │                 │               │
    │  ── ON SUCCESS ──        │                       │                       │                 │               │
    │                          │                       │                       │                 │               │
    │  POST /payments/verify   │                       │                       │                 │               │
    │  {rzp_order_id,          │                       │                       │                 │               │
    │   rzp_payment_id,        │                       │                       │                 │               │
    │   signature}             │                       │                       │                 │               │
    │─────────────────────────►│──────────────────────►│                       │                 │               │
    │                          │                       │                       │                 │               │
    │                          │                       │  Verify HMAC signature │                 │               │
    │                          │                       │                       │                 │               │
    │                          │                       │  UPDATE txs SET       │                 │               │
    │                          │                       │  status=SUCCESS       │                 │               │
    │                          │                       │  rzp_payment_id=...   │                 │               │
    │                          │                       │──────────────────────────────────────────────────────►│
    │                          │                       │                       │                 │               │
    │                          │                       │  FULFILLMENT:         │                 │               │
    │                          │                       │  Feign: Unlock        │                 │               │
    │                          │                       │  premium template     │                 │               │
    │                          │                       │──────────────────────►│                 │               │
    │                          │                       │◄──────────────────────│                 │               │
    │                          │                       │                       │                 │               │
    │                          │◄──────────────────────│                       │                 │               │
    │◄─────────────────────────│  { status: success }  │                       │                 │               │
    │                          │                       │                       │                 │               │
    │  ── WEBHOOK PATH ──      │                       │                       │                 │               │
    │                          │                       │                       │                 │               │
    │                          │                       │  POST /payments/      │                 │               │
    │                          │                       │  webhook              │                 │               │
    │                          │                       │◄───────────────────────────────────────│               │
    │                          │                       │                       │                 │               │
    │                          │                       │  Verify HMAC          │                 │               │
    │                          │                       │  Update status        │                 │               │
    │                          │                       │  Trigger fulfillment  │                 │               │
```

### 5.5 Failure Scenarios

| Scenario | Impact | Mitigation |
|---|---|---|
| **Redis down** | Cache miss → direct DB read (slower but works) | Graceful degradation; profile views served from DB directly |
| **PostgreSQL down** | All CRUD operations fail | Connection pooling, read replicas, automated failover |
| **AI API down (OpenAI)** | Bio generation fails → fallback text returned | Circuit breaker (Resilience4j) + static fallback bio |
| **Razorpay API down** | New orders cannot be created | Retry with exponential backoff, return 503 with retry header |
| **Eureka down** | New instances not discovered | Cache Eureka registry locally (fallback to last known) |
| **JWT secret rotated** | All existing sessions invalidated | Grace period with multiple signing keys (key rotation strategy) |

---

## 6. Data Architecture

### 6.1 Database per Service

| Service | Database Name | Tables | Schema Owner |
|---|---|---|---|
| auth-service | `auth_db` | `users` | auth-service only |
| card-service | `card_db` | `card_profiles` | card-service only |
| lead-service | `lead_db` | `leads` | lead-service only |
| payment-service | `payment_db` | `transactions` | payment-service only |

### 6.2 Redis Namespace Convention

```
profiles:{slug}          → Card profile JSON (TTL: 300s)
views:{slug}             → Integer view counter (no TTL - persistent)
blacklist:{jwtId}        → Blacklisted JWT IDs (TTL matches JWT expiry)
rate-limit:{ip}:{route}  → Rate limit counters (sliding window)
```

### 6.3 Cross-Service Data Access Policy

**Strict rule:** No service directly queries another service's database. All cross-service data access happens via:

1. **Internal REST APIs** (Feign clients with API key auth) — for synchronous data needs
2. **Event-driven updates** (Redis Streams) — for asynchronous eventual consistency

---

## 7. Containerization & Deployment

### 7.1 Docker Compose Topology

```yaml
version: '3.8'

services:
  # ── Infrastructure ──
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_MULTIPLE_DBS: auth_db,card_db,lead_db,payment_db
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD:-}
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  # ── Service Registry ──
  discovery-service:
    build: ./discovery-service
    ports:
      - "8761:8761"
    environment:
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # ── API Gateway ──
  gateway-service:
    build: ./gateway-service
    ports:
      - "8765:8765"
    environment:
      JWT_SECRET: ${JWT_SECRET}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    depends_on:
      discovery-service:
        condition: service_healthy
      redis:
        condition: service_healthy

  # ── Microservices ──
  auth-service:
    build: ./auth-service
    ports:
      - "8081:8081"
    environment:
      AUTH_DB_HOST: postgres
      AUTH_DB_PORT: 5432
      AUTH_DB_NAME: auth_db
      AUTH_DB_USERNAME: postgres
      AUTH_DB_PASSWORD: postgres
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      INTERNAL_API_KEY: ${INTERNAL_API_KEY}
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      discovery-service:
        condition: service_healthy

  card-service:
    build: ./card-service
    ports:
      - "8082:8082"
    environment:
      CARD_DB_HOST: postgres
      CARD_DB_PORT: 5432
      CARD_DB_NAME: card_db
      CARD_DB_USERNAME: postgres
      CARD_DB_PASSWORD: postgres
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      INTERNAL_API_KEY: ${INTERNAL_API_KEY}
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    depends_on:
      - auth-service
      - postgres
      - redis

  lead-service:
    build: ./lead-service
    ports:
      - "8083:8083"
    environment:
      LEAD_DB_HOST: postgres
      LEAD_DB_PORT: 5432
      LEAD_DB_NAME: lead_db
      LEAD_DB_USERNAME: postgres
      LEAD_DB_PASSWORD: postgres
      JWT_SECRET: ${JWT_SECRET}
      INTERNAL_API_KEY: ${INTERNAL_API_KEY}
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    depends_on:
      - card-service
      - postgres
      - redis

  ai-service:
    build: ./ai-service
    ports:
      - "8084:8084"
    environment:
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      INTERNAL_API_KEY: ${INTERNAL_API_KEY}
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      REMOVEBG_API_KEY: ${REMOVEBG_API_KEY}
      REPLICATE_API_KEY: ${REPLICATE_API_KEY}
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    depends_on:
      - redis
      - discovery-service

  payment-service:
    build: ./payment-service
    ports:
      - "8085:8085"
    environment:
      PAYMENT_DB_HOST: postgres
      PAYMENT_DB_PORT: 5432
      PAYMENT_DB_NAME: payment_db
      PAYMENT_DB_USERNAME: postgres
      PAYMENT_DB_PASSWORD: postgres
      JWT_SECRET: ${JWT_SECRET}
      INTERNAL_API_KEY: ${INTERNAL_API_KEY}
      RAZORPAY_KEY_ID: ${RAZORPAY_KEY_ID}
      RAZORPAY_KEY_SECRET: ${RAZORPAY_KEY_SECRET}
      RAZORPAY_WEBHOOK_SECRET: ${RAZORPAY_WEBHOOK_SECRET}
      EUREKA_USERNAME: ${EUREKA_USERNAME}
      EUREKA_PASSWORD: ${EUREKA_PASSWORD}
    depends_on:
      - auth-service
      - card-service
      - postgres
      - discovery-service

volumes:
  postgres-data:
  redis-data:
```

### 7.2 Parent Project Directory Structure

```
cardpro-ai/
├── docker-compose.yml
├── .env.example
├── README.md
│
├── discovery-service/          # Port 8761
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── gateway-service/            # Port 8765
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── auth-service/               # Port 8081
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── card-service/               # Port 8082
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── lead-service/               # Port 8083
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── ai-service/                 # Port 8084
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── payment-service/            # Port 8085
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
└── react-frontend/             # Vite + React 18
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile
    └── src/
```

---

## 8. Environment Variable Matrix

### 8.1 Common Variables (All Services)

| Variable | Required | Used By | Description |
|---|---|---|---|
| `EUREKA_USERNAME` | Yes | All | Eureka auth username |
| `EUREKA_PASSWORD` | Yes | All | Eureka auth password |
| `JWT_SECRET` | Yes | All (except discovery) | 256-bit HMAC secret for JWT |
| `INTERNAL_API_KEY` | Yes | All (except discovery, gateway) | Shared secret for Feign calls |

### 8.2 Service-Specific Variables

| Variable | Required | Used By | Description |
|---|---|---|---|
| `REDIS_HOST` | Yes | gateway, auth, card, ai | Redis host |
| `REDIS_PORT` | Yes | gateway, auth, card, ai | Redis port |
| `REDIS_PASSWORD` | No | gateway, auth, card, ai | Redis password |
| `AUTH_DB_HOST` | Yes | auth-service | PostgreSQL host |
| `CARD_DB_HOST` | Yes | card-service | PostgreSQL host |
| `LEAD_DB_HOST` | Yes | lead-service | PostgreSQL host |
| `PAYMENT_DB_HOST` | Yes | payment-service | PostgreSQL host |
| `OPENAI_API_KEY` | Yes | ai-service | OpenAI key |
| `REMOVEBG_API_KEY` | Yes | ai-service | Remove.bg key |
| `REPLICATE_API_KEY` | Yes | ai-service | Replicate key |
| `RAZORPAY_KEY_ID` | Yes | payment-service | Razorpay API key |
| `RAZORPAY_KEY_SECRET` | Yes | payment-service | Razorpay secret |
| `RAZORPAY_WEBHOOK_SECRET` | Yes | payment-service | Webhook HMAC secret |
| `CORS_ALLOWED_ORIGINS` | Yes | gateway-service | Frontend domain whitelist |

### 8.3 Sample .env File

```bash
# ── Eureka ──
EUREKA_USERNAME=eureka
EUREKA_PASSWORD=eureka123

# ── JWT ──
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-256-bits-long!!

# ── Internal Auth ──
INTERNAL_API_KEY=shared-secret-key-for-inter-service-communication

# ── Redis ──
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# ── PostgreSQL (same instance, different databases) ──
AUTH_DB_HOST=localhost
AUTH_DB_PORT=5432
AUTH_DB_NAME=auth_db

CARD_DB_HOST=localhost
CARD_DB_PORT=5432
CARD_DB_NAME=card_db

LEAD_DB_HOST=localhost
LEAD_DB_PORT=5432
LEAD_DB_NAME=lead_db

PAYMENT_DB_HOST=localhost
PAYMENT_DB_PORT=5432
PAYMENT_DB_NAME=payment_db

POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# ── AI Services ──
OPENAI_API_KEY=sk-proj-your-openai-key
OPENAI_MODEL=gpt-4
REMOVEBG_API_KEY=your-removebg-key
REPLICATE_API_KEY=your-replicate-key

# ── Payments ──
RAZORPAY_KEY_ID=rzp_live_your_key_id
RAZORPAY_KEY_SECRET=your_razorpay_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# ── Frontend ──
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://cardpro.ai
```

---

## 9. Security Architecture

### 9.1 Security Layers

```
Layer 1: NETWORK
├── Cloudflare/Nginx: SSL termination, DDoS protection, WAF
├── Docker internal network: Services communicate over isolated network
└── Firewall rules: Only Gateway port (8765) exposed to public

Layer 2: API GATEWAY
├── JWT Validation Filter: Validates tokens for protected routes
├── Rate Limiting: Redis-backed per-IP/per-token limits
├── CORS: Whitelist specific frontend domains only
└── Request Sanitization: Strip malicious headers

Layer 3: SERVICE
├── Spring Security: Method-level security (@PreAuthorize)
├── Internal API Key: Required for all Feign/HTTP inter-service calls
├── Input Validation: @Valid annotated DTOs
└── SQL Injection Prevention: JPA parameterized queries

Layer 4: DATA
├── PostgreSQL: Encrypted connections (SSL), strong passwords
├── Redis: Password-protected, isolated namespace
└── Secrets: Never in code, always via environment variables
```

### 9.2 Authentication Flow (Gateway JWT Validation)

```
1. Client sends request with Authorization: Bearer <jwt>
2. Gateway's JwtAuthGlobalFilter intercepts all requests
3. Filter parses JWT without signature verification (or calls auth-service for validation)
4. If valid: Extract userId, email, roles → inject as headers:
   - X-User-Id: uuid
   - X-User-Email: email
   - X-User-Roles: USER,ADMIN
   - X-Correlation-Id: uuid (new if not present)
5. If invalid: Return 401 Unauthorized immediately
6. Downstream services trust the injected headers (internal network)
```

### 9.3 Inter-Service Authentication

Every Feign client call includes an `X-Internal-API-Key` header validated by each service's `InternalApiKeyFilter`. This prevents unauthorized services from calling internal endpoints even if they are on the same network.

---

## 10. Project Directory Structure

### 10.1 Complete Parent Project Layout

```
CardPro-AI/
│
├── docs/
│   ├── SRS_CardPro_AI_v2.0.md
│   └── CardPro_AI_Microservices_Architecture_v1.0.md    ← This document
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   ├── .env.example
│   ├── nginx/
│   │   └── default.conf
│   └── monitoring/
│       ├── prometheus.yml
│       └── grafana-dashboards/
│
├── discovery-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/discovery/
│       ├── DiscoveryServiceApplication.java
│       └── config/SecurityConfig.java
│
├── gateway-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/gateway/
│       ├── GatewayServiceApplication.java
│       ├── config/
│       │   ├── RouteConfig.java
│       │   ├── CorsConfig.java
│       │   └── RateLimiterConfig.java
│       └── filter/
│           ├── JwtAuthGlobalFilter.java
│           └── CorrelationIdFilter.java
│
├── auth-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/auth/
│       ├── AuthServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   └── RedisConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   └── InternalAuthController.java
│       ├── dto/request/
│       │   ├── RegisterRequest.java
│       │   └── LoginRequest.java
│       ├── dto/response/
│       │   ├── AuthResponse.java
│       │   └── ValidationResponse.java
│       ├── entity/User.java
│       ├── repository/UserRepository.java
│       ├── service/
│       │   ├── AuthService.java
│       │   ├── JwtService.java
│       │   └── TokenBlacklistService.java
│       └── exception/GlobalExceptionHandler.java
│
├── card-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/card/
│       ├── CardServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── RedisCacheConfig.java
│       │   └── AsyncConfig.java
│       ├── controller/
│       │   ├── CardController.java
│       │   ├── AdminCardController.java
│       │   └── InternalCardController.java
│       ├── dto/
│       ├── entity/CardProfile.java
│       ├── repository/CardProfileRepository.java
│       ├── service/
│       │   ├── CardService.java
│       │   ├── CardCacheService.java
│       │   ├── SlugService.java
│       │   └── ViewCounterService.java
│       └── exception/CardException.java
│
├── lead-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/lead/
│       ├── LeadServiceApplication.java
│       ├── controller/
│       │   ├── LeadController.java
│       │   └── InternalLeadController.java
│       ├── entity/Lead.java
│       ├── repository/LeadRepository.java
│       ├── service/
│       │   ├── LeadService.java
│       │   ├── LeadCreditService.java
│       │   └── LeadEventPublisher.java
│       └── client/
│           ├── CardServiceClient.java
│           └── AuthServiceClient.java
│
├── ai-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/ai/
│       ├── AiServiceApplication.java
│       ├── config/
│       │   ├── OpenAiConfig.java
│       │   └── VisionApiConfig.java
│       ├── controller/AiController.java
│       ├── service/
│       │   ├── BioGenerationService.java
│       │   ├── PhotoEnhancementService.java
│       │   └── LeadFollowupService.java
│       ├── client/
│       │   ├── OpenAiClient.java
│       │   ├── RemoveBgClient.java
│       │   └── ReplicateClient.java
│       └── consumer/LeadCreatedConsumer.java
│
├── payment-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cardpro/payment/
│       ├── PaymentServiceApplication.java
│       ├── config/
│       │   ├── RazorpayConfig.java
│       │   └── WebhookConfig.java
│       ├── controller/
│       │   ├── PaymentController.java
│       │   ├── WebhookController.java
│       │   └── InternalPaymentController.java
│       ├── entity/Transaction.java
│       ├── repository/TransactionRepository.java
│       ├── service/
│       │   ├── PaymentService.java
│       │   ├── RazorpayClientService.java
│       │   ├── WebhookVerificationService.java
│       │   └── FulfillmentService.java
│       └── client/
│           ├── CardServiceClient.java
│           └── LeadServiceClient.java
│
└── react-frontend/
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── routes/
        ├── pages/
        │   ├── PublicCardViewer.tsx
        │   ├── Login.tsx
        │   ├── Register.tsx
        │   ├── Dashboard.tsx
        │   ├── CardEditor.tsx
        │   ├── LeadManagement.tsx
        │   ├── Analytics.tsx
        │   └── Store.tsx
        ├── components/
        │   ├── templates/
        │   ├── Layouts/
        │   ├── common/
        │   └── editor/
        ├── store/
        │   └── useProfileStore.ts              # Zustand
        ├── services/
        │   ├── api.ts                          # Axios instance
        │   ├── authService.ts
        │   ├── cardService.ts
        │   ├── leadService.ts
        │   └── paymentService.ts
        ├── hooks/
        └── utils/
            ├── vcard.ts
            └── qrcode.ts
```

---

> **End of Document — CardPro AI Microservices Architecture v1.0**
>
> *This document defines the complete service decomposition, inter-service contracts,
> deployment topology, and request flows for the production-grade microservices architecture.
> Ready for Phase 1 code generation.*
