# CardPro AI Architecture

## Architecture Overview

CardPro AI follows a microservices architecture pattern using Spring Cloud.

### Key Documents
- [Microservices Architecture](../docs/CardPro_AI_Microservices_Architecture_v1.0.md)
- [Software Requirements Specification](../docs/SRS_CardPro_AI_v2.0.md)
- [Project Structure](../docs/CardPro_AI_Complete_Project_Folder_Structure.md)

### Architecture Principles
1. **Database per Service** - Each service owns its data
2. **API Gateway** - Single entry point via Spring Cloud Gateway
3. **Service Discovery** - Eureka for dynamic registration
4. **JWT Propagation** - Gateway validates tokens, headers flow to services
5. **Redis Caching** - Cache-aside pattern for public profiles
6. **Async Events** - Redis Streams for eventual consistency

### Service Mesh
```
Frontend → Gateway → Eureka → Microservices → PostgreSQL/Redis
```
