# Product Platform

A learning project that shows how a modern, industry-style **Spring Boot microservice system** fits
together: an API gateway, service discovery, a JWT auth service and a product service that call each
other over HTTP, with an Angular frontend (login, sign-up, products).

```
 Browser (Angular, :4200)
     │  /api/**  (dev proxy)
     ▼
 api-gateway (:8090) ──── looks up services in ───► discovery-server / Eureka (:8761)
     │ lb://auth-service           lb://product-service        ▲ every service registers here
     ▼                               ▼                         │
 auth-service (:8082)  ◄──HTTP──  product-service (:8081)
  • signup / login                 • product CRUD + ownership rules
  • issues RS256 JWTs              • verifies JWTs with auth-service's public keys (JWKS)
  • /.well-known/jwks.json         • /summary calls auth-service on virtual threads
     │                               │
  auth_db (PostgreSQL)            product_db (PostgreSQL)      ← one database per service
```

## Tech stack

| Area | Choice |
|---|---|
| Language | Java 25 (LTS) |
| Framework | Spring Boot 4.0.9, Spring Cloud 2025.1.4 (Gateway, Eureka, LoadBalancer) |
| Security | Spring Security OAuth2 Resource Server, RS256 JWT, BCrypt passwords |
| Data | Spring Data JPA, PostgreSQL, Flyway migrations, optimistic locking, auditing |
| HTTP clients | Declarative `@HttpExchange` interface on `RestClient` (built into Spring 7) |
| API docs | springdoc OpenAPI / Swagger UI per service |
| Frontend | Angular 22: standalone components, signals, zoneless, functional guards/interceptors |
| Tests | JUnit 5, Mockito, `@WebMvcTest` with Spring Security; Vitest for Angular |

## Running it

**Prerequisites:** JDK 25+, Node 20+, PostgreSQL on `localhost:5432` (user `postgres`, password `7888`)
with two databases: `product_db` and `auth_db`. Tables are created automatically by Flyway.

> No local PostgreSQL? `docker compose up -d` starts one with both databases.

### One click (Windows)

```
start-all.cmd
```

### Manually: one terminal per line, in this order

```powershell
.\mvnw.cmd -pl discovery-server spring-boot:run   # 1. wait until it says "Started"
.\mvnw.cmd -pl auth-service spring-boot:run       # 2.
.\mvnw.cmd -pl product-service spring-boot:run    # 3.
.\mvnw.cmd -pl api-gateway spring-boot:run        # 4.
cd frontend; npm start                            # 5. then open http://localhost:4200
```

Everything is configurable through environment variables (`DB_PASSWORD`, `DB_URL`, `PORT`,
`EUREKA_URL`, `JWT_TTL`, ...). The values in `application.yml` are local-development defaults only.

### Useful URLs

| URL | What |
|---|---|
| http://localhost:4200 | The app |
| http://localhost:8761 | Eureka dashboard: which services are up |
| http://localhost:8082/swagger-ui.html | auth-service API docs |
| http://localhost:8081/swagger-ui.html | product-service API docs |
| http://localhost:8090/actuator/gateway/routes | Gateway routes |

### Tests

```powershell
.\mvnw.cmd verify                          # all backend tests (no database needed)
cd frontend; npx ng test --watch=false     # frontend tests
```

## API (through the gateway at `http://localhost:8090`)

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/signup` | – | `{fullName, email, password}` → 201 + token |
| POST | `/api/auth/login` | – | `{email, password}` → token |
| GET | `/api/users/me` | Bearer | Current user's profile |
| GET | `/api/products?q=&page=&size=&sort=` | Bearer | Paged, searchable list |
| GET | `/api/products/{id}` | Bearer | |
| POST | `/api/products` | Bearer | `{name, description?, price}` → 201 + `Location` |
| PUT | `/api/products/{id}` | Bearer | Owner or ADMIN only |
| DELETE | `/api/products/{id}` | Bearer | Owner or ADMIN only → 204 |
| GET | `/api/products/summary` | Bearer | Calls auth-service; runs on virtual threads |

All errors use RFC 9457 `application/problem+json`, e.g. 400 responses carry an `errors` map of field → message.

## Where to look when learning

| Concept | File |
|---|---|
| Virtual threads (per request) | `spring.threads.virtual.enabled` in both services' `application.yml` |
| Virtual threads (explicit fan-out) | `product-service/.../product/ProductSummaryService.java` |
| Records: DTOs | `*/dto/*.java`, `UserResponse`, `UserProfile`, `PageResponse`, `CurrentUser` |
| Records: configuration | `JwtProperties`, `AuthServiceProperties` (`@ConfigurationProperties`) |
| Service-to-service call | `product-service/.../client/UserClient.java` + `config/HttpClientConfig.java` |
| Loose coupling | No shared code between services; each owns its DTOs and its database |
| Graceful degradation | `ProductSummaryService` still answers if auth-service is down |
| Issuing / verifying JWTs | `auth-service/.../TokenService.java`, `JwtKeyConfig.java`; `product-service/.../JwtDecoderConfig.java` |
| Schema migrations | `*/src/main/resources/db/migration/` |
| Gateway routing | `api-gateway/src/main/resources/application.yml` |

## Extending with Spring AI / RAG

The layout is ready for an AI module:

1. Add a module, e.g. `ai-service`, to the root `pom.xml` (copy `product-service/pom.xml` as a start)
   and add Spring AI's BOM plus a model starter (Anthropic, OpenAI, Ollama, ...) and `spring-ai-starter-vector-store-pgvector`.
2. Register it with Eureka and add a gateway route such as `Path=/api/ai/**` → `lb://ai-service`.
3. Product `description` is the natural text to embed: index products into pgvector, then answer
   questions like *"which of my products are good gifts under $50?"* with retrieval-augmented generation.
4. Reuse the patterns here: verify JWTs via JWKS, call product-service through an `@HttpExchange` client,
   and run slow model/tool calls on virtual threads.

## Deliberate simplifications (good next steps)

- **Signing key is generated at startup.** Restarting auth-service logs everyone out. Production apps load
  it from a secret store and rotate keys through the JWK set.
- **Token stored in `localStorage`.** Simple, but readable by page scripts. A backend-for-frontend with
  HttpOnly cookies is the stronger pattern.
- **No message broker.** Services talk synchronously over HTTP. Adding Kafka or RabbitMQ (for example a
  `UserRegistered` event) is the next step towards fully asynchronous loose coupling.
- **Unit and slice tests only.** Add Testcontainers for real PostgreSQL integration tests once Docker is running.
- **Observability.** Add Micrometer tracing (OpenTelemetry) to follow one request across the gateway and services.
