# CardPro AI — Complete Project Folder Structure

## Production-Grade Microservices + React Frontend

| Project | CardPro AI |
|---|---|
| Version | 1.0.0 |
| Date | July 27, 2026 |
| Architecture | Microservices (Spring Cloud) |

---

## Root Level

```
cardpro-ai/
│
├── BACKEND/
├── FRONTEND/
├── DATABASE/
├── DOCKER/
├── POSTMAN/
├── GITHUB/
├── DOCS/
│
├── .gitignore
├── .gitattributes
├── README.md
├── LICENSE
└── docker-compose.yml
```

---

## 1. BACKEND/ — Microservices

```
BACKEND/
│
├── pom.xml                                    # Parent POM (multi-module)
│
├── discovery-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/discovery/
│       │   │   ├── DiscoveryServiceApplication.java
│       │   │   │
│       │   │   └── config/
│       │   │       └── SecurityConfig.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── bootstrap.yml
│       │
│       └── test/
│           └── java/com/cardpro/discovery/
│               └── DiscoveryServiceApplicationTests.java
│
├── gateway-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/gateway/
│       │   │   ├── GatewayServiceApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── RouteConfig.java
│       │   │   │   ├── CorsConfig.java
│       │   │   │   └── RateLimiterConfig.java
│       │   │   │
│       │   │   ├── filter/
│       │   │   │   ├── JwtAuthGlobalFilter.java
│       │   │   │   └── CorrelationIdFilter.java
│       │   │   │
│       │   │   ├── util/
│       │   │   │   └── JwtUtil.java
│       │   │   │
│       │   │   └── exception/
│       │   │       └── GatewayExceptionHandler.java
│       │   │
│       │   └── resources/
│       │       └── application.yml
│       │
│       └── test/
│           └── java/com/cardpro/gateway/
│               ├── GatewayServiceApplicationTests.java
│               └── filter/
│                   └── JwtAuthGlobalFilterTest.java
│
├── auth-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/auth/
│       │   │   ├── AuthServiceApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   └── RedisConfig.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   └── InternalAuthController.java
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── RegisterRequest.java
│       │   │   │   │   ├── LoginRequest.java
│       │   │   │   │   └── RefreshTokenRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── AuthResponse.java
│       │   │   │       ├── UserResponse.java
│       │   │   │       └── ValidationResponse.java
│       │   │   │
│       │   │   ├── entity/
│       │   │   │   └── User.java
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   └── UserRepository.java
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── JwtService.java
│       │   │   │   └── TokenBlacklistService.java
│       │   │   │
│       │   │   ├── security/
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   └── InternalApiKeyFilter.java
│       │   │   │
│       │   │   ├── mapper/
│       │   │   │   └── UserMapper.java
│       │   │   │
│       │   │   ├── util/
│       │   │   │   └── CookieUtil.java
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── AuthException.java
│       │   │       ├── UserAlreadyExistsException.java
│       │   │       ├── InvalidCredentialsException.java
│       │   │       └── TokenExpiredException.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__create_users_table.sql
│       │           └── V2__add_user_roles.sql
│       │
│       └── test/
│           └── java/com/cardpro/auth/
│               ├── AuthServiceApplicationTests.java
│               ├── controller/
│               │   └── AuthControllerTest.java
│               ├── service/
│               │   ├── AuthServiceTest.java
│               │   └── JwtServiceTest.java
│               └── repository/
│                   └── UserRepositoryTest.java
│
├── card-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/card/
│       │   │   ├── CardServiceApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── RedisCacheConfig.java
│       │   │   │   └── AsyncConfig.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── CardController.java
│       │   │   │   ├── TemplateController.java
│       │   │   │   ├── AdminCardController.java
│       │   │   │   └── InternalCardController.java
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── CreateCardRequest.java
│       │   │   │   │   └── UpdateCardRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── CardResponse.java
│       │   │   │       ├── PublicCardResponse.java
│       │   │   │       ├── TemplateResponse.java
│       │   │   │       └── CardListResponse.java
│       │   │   │
│       │   │   ├── entity/
│       │   │   │   └── CardProfile.java
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   ├── CardProfileRepository.java
│       │   │   │   └── CustomCardProfileRepository.java
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── CardService.java
│       │   │   │   ├── CardCacheService.java
│       │   │   │   ├── SlugService.java
│       │   │   │   ├── ViewCounterService.java
│       │   │   │   ├── TemplateService.java
│       │   │   │   └── AvatarService.java
│       │   │   │
│       │   │   ├── client/
│       │   │   │   ├── AuthServiceClient.java
│       │   │   │   └── PaymentServiceClient.java
│       │   │   │
│       │   │   ├── security/
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   └── InternalApiKeyFilter.java
│       │   │   │
│       │   │   ├── mapper/
│       │   │   │   └── CardMapper.java
│       │   │   │
│       │   │   ├── event/
│       │   │   │   └── ViewEventPublisher.java
│       │   │   │
│       │   │   ├── util/
│       │   │   │   └── SlugUtil.java
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── CardException.java
│       │   │       ├── SlugAlreadyExistsException.java
│       │   │       ├── CardNotFoundException.java
│       │   │       └── InvalidSlugException.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__create_card_profiles_table.sql
│       │           └── V2__add_template_index.sql
│       │
│       └── test/
│           └── java/com/cardpro/card/
│               ├── CardServiceApplicationTests.java
│               ├── controller/
│               │   └── CardControllerTest.java
│               ├── service/
│               │   ├── CardServiceTest.java
│               │   ├── CardCacheServiceTest.java
│               │   └── SlugServiceTest.java
│               └── repository/
│                   └── CardProfileRepositoryTest.java
│
├── lead-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/lead/
│       │   │   ├── LeadServiceApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   └── AsyncConfig.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── LeadController.java
│       │   │   │   ├── AdminLeadController.java
│       │   │   │   └── InternalLeadController.java
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── SubmitLeadRequest.java
│       │   │   │   │   └── CreditAdjustRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── LeadResponse.java
│       │   │   │       ├── LeadFollowupResponse.java
│       │   │   │       └── LeadPageResponse.java
│       │   │   │
│       │   │   ├── entity/
│       │   │   │   └── Lead.java
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   └── LeadRepository.java
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── LeadService.java
│       │   │   │   ├── LeadCreditService.java
│       │   │   │   └── LeadEventPublisher.java
│       │   │   │
│       │   │   ├── client/
│       │   │   │   ├── CardServiceClient.java
│       │   │   │   └── AuthServiceClient.java
│       │   │   │
│       │   │   ├── security/
│       │   │   │   └── InternalApiKeyFilter.java
│       │   │   │
│       │   │   ├── mapper/
│       │   │   │   └── LeadMapper.java
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── LeadException.java
│       │   │       ├── LeadNotFoundException.java
│       │   │       └── InsufficientCreditsException.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__create_leads_table.sql
│       │           └── V2__add_lead_indexes.sql
│       │
│       └── test/
│           └── java/com/cardpro/lead/
│               ├── LeadServiceApplicationTests.java
│               ├── controller/
│               │   └── LeadControllerTest.java
│               ├── service/
│               │   ├── LeadServiceTest.java
│               │   └── LeadCreditServiceTest.java
│               └── repository/
│                   └── LeadRepositoryTest.java
│
├── ai-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/ai/
│       │   │   ├── AiServiceApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── OpenAiConfig.java
│       │   │   │   ├── VisionApiConfig.java
│       │   │   │   ├── AsyncConfig.java
│       │   │   │   └── CircuitBreakerConfig.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── AiController.java
│       │   │   │   └── InternalAiController.java
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── BioGenerationRequest.java
│       │   │   │   │   ├── PhotoUpscaleRequest.java
│       │   │   │   │   └── LeadFollowupRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── BioGenerationResponse.java
│       │   │   │       ├── PhotoUpscaleResponse.java
│       │   │   │       └── LeadFollowupResponse.java
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── BioGenerationService.java
│       │   │   │   ├── PhotoEnhancementService.java
│       │   │   │   ├── LeadFollowupService.java
│       │   │   │   └── AiFallbackService.java
│       │   │   │
│       │   │   ├── client/
│       │   │   │   ├── OpenAiClient.java
│       │   │   │   ├── RemoveBgClient.java
│       │   │   │   ├── ReplicateClient.java
│       │   │   │   ├── PaymentServiceClient.java
│       │   │   │   ├── LeadServiceClient.java
│       │   │   │   └── AuthServiceClient.java
│       │   │   │
│       │   │   ├── consumer/
│       │   │   │   └── LeadCreatedConsumer.java
│       │   │   │
│       │   │   ├── security/
│       │   │   │   └── InternalApiKeyFilter.java
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── AiServiceException.java
│       │   │       ├── AiApiException.java
│       │   │       ├── AiPaymentRequiredException.java
│       │   │       └── AiRateLimitException.java
│       │   │
│       │   └── resources/
│       │       └── application.yml
│       │
│       └── test/
│           └── java/com/cardpro/ai/
│               ├── AiServiceApplicationTests.java
│               ├── controller/
│               │   └── AiControllerTest.java
│               ├── service/
│               │   ├── BioGenerationServiceTest.java
│               │   ├── PhotoEnhancementServiceTest.java
│               │   └── LeadFollowupServiceTest.java
│               └── client/
│                   ├── OpenAiClientTest.java
│                   └── RemoveBgClientTest.java
│
├── payment-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/cardpro/payment/
│       │   │   ├── PaymentServiceApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── RazorpayConfig.java
│       │   │   │   └── WebhookConfig.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── PaymentController.java
│       │   │   │   ├── WebhookController.java
│       │   │   │   ├── AdminPaymentController.java
│       │   │   │   └── InternalPaymentController.java
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── CreateOrderRequest.java
│       │   │   │   │   ├── VerifyPaymentRequest.java
│       │   │   │   │   └── RefundRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── CreateOrderResponse.java
│       │   │   │       ├── VerifyPaymentResponse.java
│       │   │   │       ├── TransactionResponse.java
│       │   │   │       └── TransactionPageResponse.java
│       │   │   │
│       │   │   ├── entity/
│       │   │   │   └── Transaction.java
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   └── TransactionRepository.java
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── PaymentService.java
│       │   │   │   ├── RazorpayClientService.java
│       │   │   │   ├── WebhookVerificationService.java
│       │   │   │   └── FulfillmentService.java
│       │   │   │
│       │   │   ├── client/
│       │   │   │   ├── CardServiceClient.java
│       │   │   │   ├── LeadServiceClient.java
│       │   │   │   └── AuthServiceClient.java
│       │   │   │
│       │   │   ├── security/
│       │   │   │   └── InternalApiKeyFilter.java
│       │   │   │
│       │   │   ├── mapper/
│       │   │   │   └── TransactionMapper.java
│       │   │   │
│       │   │   ├── enums/
│       │   │   │   ├── ItemType.java
│       │   │   │   └── TransactionStatus.java
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── PaymentException.java
│       │   │       ├── WebhookVerificationException.java
│       │   │       ├── PaymentVerificationException.java
│       │   │       └── OrderNotFoundException.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__create_transactions_table.sql
│       │           └── V2__add_transaction_indexes.sql
│       │
│       └── test/
│           └── java/com/cardpro/payment/
│               ├── PaymentServiceApplicationTests.java
│               ├── controller/
│               │   ├── PaymentControllerTest.java
│               │   └── WebhookControllerTest.java
│               ├── service/
│               │   ├── PaymentServiceTest.java
│               │   ├── WebhookVerificationServiceTest.java
│               │   └── FulfillmentServiceTest.java
│               └── repository/
│                   └── TransactionRepositoryTest.java
│
└── shared-lib/                                # Optional: Shared DTOs/Utils (JAR)
    ├── pom.xml
    └── src/main/java/com/cardpro/shared/
        ├── dto/
        │   ├── ApiResponse.java
        │   └── PagedResponse.java
        ├── exception/
        │   └── BaseException.java
        └── util/
            └── DateUtil.java
```

---

## 2. FRONTEND/ — React 18 + Vite + TypeScript

```
FRONTEND/
│
├── package.json
├── package-lock.json
├── tsconfig.json
├── tsconfig.node.json
├── vite.config.ts
├── index.html
├── .env.example
├── .env.development
├── .env.production
├── .eslintrc.cjs
├── .prettierrc
├── tailwind.config.js
├── postcss.config.js
├── nginx.conf                                    # Nginx config for production deployment
├── Dockerfile
│
└── src/
    ├── main.tsx                                  # React entry point
    ├── App.tsx                                   # Root component with Router
    ├── index.css                                 # Global styles + Tailwind imports
    ├── vite-env.d.ts
    │
    ├── routes/
    │   ├── index.tsx                             # Route definitions
    │   ├── PrivateRoute.tsx                      # Auth guard wrapper
    │   └── AdminRoute.tsx                        # Admin role guard
    │
    ├── pages/
    │   ├── public/
    │   │   ├── CardViewer.tsx                    # Public card profile page
    │   │   └── LeadModal.tsx                     # Lead capture modal
    │   │
    │   ├── auth/
    │   │   ├── LoginPage.tsx
    │   │   ├── RegisterPage.tsx
    │   │   └── ForgotPasswordPage.tsx
    │   │
    │   ├── dashboard/
    │   │   ├── DashboardLayout.tsx               # Sidebar + Main content layout
    │   │   ├── DashboardHome.tsx                 # Analytics overview
    │   │   ├── CardEditorPage.tsx                # Split-screen editor
    │   │   ├── LeadManagementPage.tsx            # Leads data grid
    │   │   ├── AnalyticsPage.tsx                 # Charts & metrics
    │   │   ├── StorePage.tsx                     # Upgrade store
    │   │   └── SettingsPage.tsx                  # Account settings
    │   │
    │   └── admin/
    │       ├── AdminDashboard.tsx
    │       ├── UserManagement.tsx
    │       ├── TransactionList.tsx
    │       └── ProfileModeration.tsx
    │
    ├── components/
    │   ├── templates/                             # Card templates (map of template_id → component)
    │   │   ├── BasicTemplate.tsx
    │   │   ├── PremiumTemplate1.tsx
    │   │   ├── PremiumTemplate2.tsx
    │   │   └── TemplateRenderer.tsx              # Dynamic template selector
    │   │
    │   ├── editor/                                # Admin editor components
    │   │   ├── EditorPanel.tsx                   # Left side: form inputs
    │   │   ├── MobilePreview.tsx                 # Right side: live preview
    │   │   ├── PersonalInfoForm.tsx
    │   │   ├── SkillsInput.tsx
    │   │   ├── SocialLinksInput.tsx
    │   │   ├── ServicesSection.tsx
    │   │   ├── TestimonialsSection.tsx
    │   │   ├── GalleryUpload.tsx
    │   │   ├── ThemeColorPicker.tsx
    │   │   └── AiBioGenerator.tsx               # "Generate AI Bio" button + modal
    │   │
    │   ├── layout/
    │   │   ├── Navbar.tsx
    │   │   ├── Sidebar.tsx
    │   │   ├── Footer.tsx
    │   │   └── PageContainer.tsx
    │   │
    │   ├── common/
    │   │   ├── Button.tsx
    │   │   ├── Input.tsx
    │   │   ├── Modal.tsx
    │   │   ├── Spinner.tsx
    │   │   ├── Toast.tsx
    │   │   ├── Avatar.tsx
    │   │   ├── Badge.tsx
    │   │   ├── Card.tsx
    │   │   ├── DataTable.tsx
    │   │   ├── Pagination.tsx
    │   │   ├── EmptyState.tsx
    │   │   └── ErrorBoundary.tsx
    │   │
    │   └── lead/
    │       ├── LeadCaptureModal.tsx
    │       ├── LeadTable.tsx
    │       └── FollowUpTemplate.tsx
    │
    ├── store/                                     # Zustand state management
    │   ├── useAuthStore.ts
    │   ├── useProfileStore.ts
    │   ├── useEditorStore.ts                     # Real-time editor state
    │   └── useUIStore.ts
    │
    ├── services/                                  # API service layer
    │   ├── api.ts                                 # Axios instance with interceptors
    │   ├── authService.ts
    │   ├── cardService.ts
    │   ├── leadService.ts
    │   ├── aiService.ts
    │   └── paymentService.ts
    │
    ├── hooks/
    │   ├── useAuth.ts
    │   ├── useCard.ts
    │   ├── useLeads.ts
    │   ├── useAnalytics.ts
    │   ├── useDebounce.ts
    │   └── useMediaQuery.ts
    │
    ├── types/
    │   ├── user.ts
    │   ├── card.ts
    │   ├── lead.ts
    │   ├── transaction.ts
    │   ├── analytics.ts
    │   └── api.ts                                 # ApiResponse<T> generic types
    │
    ├── utils/
    │   ├── vcard.ts                               # Client-side .vcf file generator
    │   ├── qrcode.ts                              # QR code generation wrapper
    │   ├── upi.ts                                 # UPI deep-link builder
    │   ├── formatters.ts                          # Date, currency formatters
    │   ├── validators.ts                          # Form validation rules
    │   └── constants.ts                           # App-wide constants
    │
    └── assets/
        ├── images/
        │   ├── logo.svg
        │   ├── logo-dark.svg
        │   ├── favicon.ico
        │   └── og-image.png
        ├── fonts/
        └── icons/
```

---

## 3. DATABASE/ — SQL Schemas, Migrations & Scripts

```
DATABASE/
│
├── schemas/
│   ├── 01_create_databases.sql                   # CREATE DATABASE statements for all 4 DBs
│   ├── 02_auth_schema.sql                        # Full auth_db schema
│   ├── 03_card_schema.sql                        # Full card_db schema
│   ├── 04_lead_schema.sql                        # Full lead_db schema
│   └── 05_payment_schema.sql                     # Full payment_db schema
│
├── migrations/
│   ├── auth-service/
│   │   ├── V1__create_users_table.sql
│   │   └── V2__add_user_roles.sql
│   ├── card-service/
│   │   ├── V1__create_card_profiles_table.sql
│   │   └── V2__add_template_index.sql
│   ├── lead-service/
│   │   ├── V1__create_leads_table.sql
│   │   └── V2__add_lead_indexes.sql
│   └── payment-service/
│       ├── V1__create_transactions_table.sql
│       └── V2__add_transaction_indexes.sql
│
├── seeds/
│   ├── seed_users.sql                            # Admin user for development
│   ├── seed_templates.sql                        # Default template configurations
│   └── seed_demo_data.sql                        # Demo profiles + leads for testing
│
├── indexes/
│   ├── auth_indexes.sql
│   ├── card_indexes.sql
│   ├── lead_indexes.sql
│   └── payment_indexes.sql
│
├── scripts/
│   ├── init-multiple-databases.sh                # Shell script to create all DBs
│   ├── backup.sh                                 # Automated backup script
│   ├── restore.sh                                # Restore from backup
│   └── reset-dev.sh                              # Reset development databases
│
└── README.md
```

---

## 4. DOCKER/ — Containerization & Deployment

```
DOCKER/
│
├── dev/
│   ├── docker-compose.yml                        # Local development (all services)
│   └── docker-compose.override.yml               # Dev overrides (volumes, ports)
│
├── prod/
│   ├── docker-compose.yml                        # Production composition
│   ├── docker-compose.monitoring.yml             # Prometheus + Grafana
│   └── docker-compose.logging.yml                # ELK Stack / Loki + Grafana
│
├── images/
│   ├── postgres/
│   │   ├── Dockerfile                            # Custom PostgreSQL with init scripts
│   │   └── init-databases.sh                     # Multi-database creation script
│   └── redis/
│       └── redis.conf                            # Custom Redis configuration
│
├── nginx/
│   ├── default.conf                              # Reverse proxy config
│   ├── nginx.conf                                # Main nginx config
│   └── ssl/
│       ├── cardpro.ai.crt
│       └── cardpro.ai.key
│
├── monitoring/
│   ├── prometheus/
│   │   ├── prometheus.yml
│   │   └── alert-rules.yml
│   └── grafana/
│       ├── dashboards/
│       │   ├── service-metrics.json
│       │   └── business-metrics.json
│       └── datasources.yml
│
├── scripts/
│   ├── deploy.sh                                 # Deployment script
│   ├── healthcheck.sh                            # Service health verification
│   ├── migrate.sh                                # Flyway migration runner
│   └── seed.sh                                   # Seed data loader
│
└── .dockerignore
```

---

## 5. POSTMAN/ — API Collections & Environments

```
POSTMAN/
│
├── collections/
│   ├── CardPro AI - Auth.json                    # Auth endpoints collection
│   ├── CardPro AI - Cards.json                   # Card/Profile endpoints
│   ├── CardPro AI - Leads.json                   # Lead endpoints
│   ├── CardPro AI - AI.json                      # AI service endpoints
│   ├── CardPro AI - Payments.json                # Payment endpoints
│   ├── CardPro AI - Admin.json                   # Admin endpoints
│   └── CardPro AI - Complete.json                # Complete merged collection
│
├── environments/
│   ├── Local.postman_environment.json            # http://localhost:8765
│   ├── Staging.postman_environment.json          # https://staging-api.cardpro.ai
│   └── Production.postman_environment.json       # https://api.cardpro.ai
│
└── README.md                                     # Setup instructions
```

---

## 6. GITHUB/ — CI/CD, Templates & Automation

```
GITHUB/
│
├── workflows/
│   ├── ci.yml                                    # Build + Test all services
│   ├── cd-staging.yml                            # Deploy to staging
│   ├── cd-production.yml                         # Deploy to production (manual trigger)
│   ├── docker-build.yml                          # Build and push Docker images
│   ├── codeql-analysis.yml                       # Security scanning
│   ├── dependency-review.yml                     # Dependency vulnerability check
│   └── sonarcloud.yml                            # Code quality analysis
│
├── ISSUE_TEMPLATE/
│   ├── bug_report.md
│   ├── feature_request.md
│   └── task.md
│
├── PULL_REQUEST_TEMPLATE/
│   └── pull_request_template.md
│
├── CODEOWNERS                                     # Auto-assign reviewers
├── CONTRIBUTING.md                                # Contribution guidelines
├── SECURITY.md                                    # Security policy
└── dependabot.yml                                 # Automated dependency updates
```

---

## 7. DOCS/ — Project Documentation

```
DOCS/
│
├── SRS_CardPro_AI_v2.0.md                         # Software Requirements Specification
├── CardPro_AI_Microservices_Architecture_v1.0.md  # Microservices Architecture Design
├── CardPro_AI_Complete_Project_Folder_Structure.md # This document
│
├── api/
│   ├── auth-api.md
│   ├── card-api.md
│   ├── lead-api.md
│   ├── ai-api.md
│   ├── payment-api.md
│   └── admin-api.md
│
├── database/
│   ├── er-diagram.md
│   ├── schema-details.md
│   └── data-flow.md
│
├── deployment/
│   ├── local-setup.md
│   ├── staging-deployment.md
│   └── production-deployment.md
│
├── guides/
│   ├── developer-onboarding.md
│   ├── coding-standards.md
│   ├── testing-guide.md
│   └── security-checklist.md
│
├── diagrams/
│   ├── architecture.png
│   ├── deployment-topology.png
│   └── request-flows.png
│
└── README.md
```

---

## 8. Configuration & Root Files

```
cardpro-ai/                                        # ROOT
│
├── .gitignore                                     # Java, Node, Docker, IDE ignores
├── .gitattributes                                 # Git LFS for large files
├── .env.example                                   # All environment variables template
├── .env.development                               # Dev environment defaults
├── .env.staging                                   # Staging environment
├── README.md                                      # Project overview
├── LICENSE                                        # MIT / Proprietary
├── CONTRIBUTING.md                                # How to contribute
│
├── docker-compose.yml                             # Root docker-compose (all services)
├── docker-compose.prod.yml                        # Production variant
├── docker-compose.monitoring.yml                  # Monitoring stack
│
└── Makefile                                       # Convenience commands (build, test, deploy)
```

---

## 9. Summary: File Count by Category

| Category | Approximate Files |
|---|---|
| **Backend (Java)** | 180+ Java files + 12 resource files |
| **Frontend (React/TS)** | 60+ TypeScript/TSX files + config files |
| **Database** | 15 SQL files + shell scripts |
| **Docker** | 10+ Docker/Compose/Nginx files |
| **Postman** | 9 collection/environment files |
| **GitHub** | 10+ workflow/template files |
| **Documentation** | 15+ Markdown files |
| **Configuration** | 10+ root config files |
| **Total** | **~320+ files** |

---

> **End of Document — CardPro AI Complete Project Folder Structure**
>
> *This structure is ready for automated code generation across all 7 microservices,
> the React frontend, and all supporting infrastructure.*
