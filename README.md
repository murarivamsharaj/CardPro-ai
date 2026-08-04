<div align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3"/>
  <img src="https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React 18"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 15"/>
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis 7"/>
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