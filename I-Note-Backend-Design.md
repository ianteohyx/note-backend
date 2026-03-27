# I-Note Backend Design

> Personal reference for interviews and presentations — covers architecture, security, performance, and design decisions behind the I-Note collaborative note-taking REST API.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Architecture](#architecture)
3. [Database Design](#database-design)
4. [Security Design](#security-design)
5. [API Design](#api-design)
6. [Performance & Efficiency](#performance--efficiency)
7. [Transaction Management](#transaction-management)
8. [Exception Handling](#exception-handling)
9. [Configuration & Environment](#configuration--environment)
10. [AWS Hosting](#aws-hosting)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.5 |
| Database | MySQL 8 (AWS RDS) |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + jjwt 0.12.6 |
| Connection Pool | HikariCP (built into Spring Boot) |
| Rate Limiting | Bucket4j 8.10.1 |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven |

---

## Architecture

### Pattern: Generic Service Layer

Every business operation is encapsulated in its own service class that extends a shared abstract base:

```
Controller → Service<Request, Response> → Repository → MySQL
```

```java
public abstract class Service<Request extends ApiRequest, Response extends ApiResponse> {
    public Response execute(Request request)       // entry point from controller
    public abstract Response doService(Request)    // implement business logic here
    public User getUserUsingTheService()           // gets current authenticated user
    protected void assertIsOwner(Note note)        // centralized auth check
    protected void assertIsRecipient(SharedNote)   // centralized auth check
    protected void assertHasWritePermission(SharedNote) // centralized auth check
}
```

**Why this matters:**
- Every service is a single-responsibility class — easy to test, easy to extend
- Fully type-safe: `Service<AddNoteRequest, ApiResponse>` — compiler enforces correct request/response types
- Authorization logic centralized in base class — no duplicated `if (!owner.equals(user)) throw` scattered across services
- New services follow a consistent, predictable pattern

### Package Structure

```
com.yx.note_app/
├── config/        - Security config, scheduled tasks
├── controllers/   - HTTP layer only, no business logic
├── dto/           - Data transfer objects (what gets serialized to JSON)
├── enums/         - Permission (READ/WRITE), ResponseOutcome
├── exception/     - Custom exceptions + global handler
├── models/        - JPA entities
├── repositories/  - DB queries
├── security/      - JWT filter, rate limiting filter, auth service
├── services/
│   ├── request/   - Input POJOs per service
│   ├── response/  - Output POJOs per service
│   └── service/   - Business logic (one class per operation)
└── utils/         - JWT utils, model-to-DTO mappers
```

**Key design principle:** Controllers only handle HTTP concerns (status codes, request parsing). All business logic lives in services. All DB access lives in repositories.

---

## Database Design

### Entity Relationship

```
User (1) ──────< Note (many)
User (1) ──────< SharedNote (many)
Note (1) ──────< SharedNote (many)
User (1) ──────< RefreshToken (many)
```

### Tables

**users**
```
id          INT PK AUTO_INCREMENT
username    VARCHAR UNIQUE NOT NULL
password    VARCHAR NOT NULL  (BCrypt hashed, never plain text)
```

**notes**
```
id            INT PK AUTO_INCREMENT
title         VARCHAR NOT NULL
content       TEXT
authorId      INT FK → users.id
dateCreated   DATETIME
dateModified  DATETIME
```

**sharednotes**
```
id                INT PK AUTO_INCREMENT
note_id           INT FK → notes.id
shared_to_user_id INT FK → users.id
permission        ENUM('READ', 'WRITE') NOT NULL
UNIQUE(note_id, shared_to_user_id)     ← prevents duplicate shares
```

**refreshtokens**
```
token      VARCHAR PK  (UUID)
user_id    INT FK → users.id
expiryDate DATETIME
revoked    BOOLEAN
createdAt  DATETIME
```

### Naming Strategy

Uses `PhysicalNamingStrategyStandardImpl` — column and table names map **literally** from entity field names (no automatic snake_case conversion). What you name it in Java is exactly what exists in MySQL.

### Relationships & Fetch Strategy

All `@ManyToOne` relations are `FetchType.LAZY` — nothing is loaded until explicitly accessed. This is intentional and correct — loading is controlled through `JOIN FETCH` in queries rather than letting Hibernate decide.

---

## Security Design

### Authentication Flow

```
1. POST /api/users/login
   → validate credentials (BCrypt password check)
   → generate JWT (15 min expiry)
   → generate Refresh Token (7 days, stored in DB)
   → return both to client

2. Every protected request
   → client sends: Authorization: Bearer <JWT>
   → JwtAuthenticationFilter validates JWT
   → sets SecurityContext with authenticated user
   → request proceeds to controller

3. POST /api/users/refresh (when JWT expires)
   → client sends refresh token
   → validate: not expired, not revoked, exists in DB
   → revoke old token, issue new refresh token (rotation)
   → issue new JWT
   → return both
```

### JWT

- Library: `jjwt` 0.12.6
- Expiry: **15 minutes** — short-lived intentionally, limits damage if stolen
- Signing key: `${JWT_SECRET}` — Base64-encoded, stored as env var (never in code)
- Stateless — server doesn't store JWTs, validated by signature only

### Refresh Token Security — Token Rotation + Revoke-All

This is the most sophisticated part of the security design:

**Token Rotation:** Every `/refresh` call invalidates the old token and issues a brand new one. A refresh token can only be used once.

**Revoke-All on Reuse Detection:** If a refresh token that has already been used (revoked) is presented again — this signals a **stolen token attack**. The system immediately revokes ALL refresh tokens for that user, forcing a full re-login. This is the industry-standard defense against token theft.

```java
if (refreshToken.isRevoked()) {
    refreshTokenRepository.revokeAllByUser(user); // security response
    throw InvalidRefreshTokenException.revoked();
}
```

The `@Transactional(noRollbackFor = InvalidRefreshTokenException.class)` ensures the revoke-all write **commits to the DB even though an exception is thrown** — critical for this security measure to actually work.

**Cleanup:** Expired tokens are purged daily at 3 AM via `@Scheduled` task.

### Password Security

- BCrypt hashing via Spring Security's `PasswordEncoder`
- Passwords are **never stored in plain text**, never logged
- Sensitive fields are excluded from logging

### Rate Limiting

- Library: Bucket4j (token bucket algorithm)
- Applied to: `/signup`, `/login`, `/refresh` — the auth endpoints most vulnerable to brute force
- Limit: **10 requests/minute per IP**
- IP detection: `X-Forwarded-For` header (handles reverse proxy / load balancer) with fallback to remote address
- Returns HTTP 429 with `RATE_LIMIT_EXCEEDED` outcome on breach

### Spring Security Filter Chain

Filters execute in this order on every request:

```
1. RateLimitingFilter     → blocks before auth if rate exceeded
2. JwtAuthenticationFilter → validates JWT, sets SecurityContext
3. Spring Security filters → enforces endpoint access rules
```

### CORS

- Configured per environment — `http://localhost:3000` in dev, explicit `${CORS_ALLOWED_ORIGINS}` in prod
- Only the known frontend origin can make credentialed cross-origin requests
- Credentials: `true` (required for cookie/auth header support)

### Authorization — Entity-Level Ownership

Beyond authentication (who are you), the app enforces authorization (what can you do):

- Only a note's **author** can update, delete, share, unshare, or change permissions on it
- Only the **recipient** of a shared note can view or edit it (edit requires WRITE permission)
- Checks are centralized in the base `Service` class — not duplicated per service

---

## API Design

### RESTful Conventions

- Resources are nouns: `/api/notes`, `/api/shares`
- HTTP methods convey action: `GET` read, `POST` create, `PATCH` update, `DELETE` remove
- Correct status codes: `201` for creates, `200` for reads/updates, `404` not found, `403` forbidden, `409` conflict, `429` rate limited
- All responses follow a consistent envelope with `ResponseOutcome` (success flag, code, HTTP status)

### Endpoints Summary

| Method | Path | Who can call |
|--------|------|-------------|
| POST | `/api/users/signup` | Public |
| POST | `/api/users/login` | Public |
| POST | `/api/users/refresh` | Public |
| POST | `/api/notes` | Authenticated |
| GET | `/api/notes` | Authenticated (own notes, paginated) |
| GET | `/api/notes/{id}` | Owner only |
| PATCH | `/api/notes/{id}` | Owner only |
| DELETE | `/api/notes/{id}` | Owner only |
| POST | `/api/shares` | Note owner only |
| GET | `/api/shares/received` | Authenticated (paginated) |
| GET | `/api/shares/{id}` | Recipient only |
| PATCH | `/api/shares/{id}` | Recipient with WRITE permission only |
| GET | `/api/shares/note/{noteId}/users` | Note owner only |
| DELETE | `/api/shares/note/{noteId}/user/{username}` | Note owner only |
| PATCH | `/api/shares/note/{noteId}/user/{username}/permission` | Note owner only |

### Pagination

List endpoints (`GET /api/notes`, `GET /api/shares/received`) are paginated using Spring Data's `Pageable`. Response includes `page`, `size`, `totalElements`, `totalPages` — clients can navigate large datasets without loading everything.

### API Documentation

Swagger UI auto-generated via SpringDoc OpenAPI. **Disabled in prod** (security) — only accessible in dev at `/swagger-ui.html`.

---

## Performance & Efficiency

### N+1 Query Problem — Solved

**What it is:** Fetching N records then firing 1 additional query per record to load a relation = N+1 total queries instead of 1.

**Example without fix:** Fetch 10 notes (1 query), then access `note.getAuthor().getUsername()` for each → 10 more queries = 11 total.

**Fix:** `JOIN FETCH` in JPQL queries loads related entities in the same SQL query:

```java
// NoteRepository — fetches author in same query as notes
@Query("SELECT n FROM Note n JOIN FETCH n.author WHERE n.author = :author")
Page<Note> findByAuthor(@Param("author") User author, Pageable pageable);

// ShareNoteRepository — fetches note, note.author, sharedToUser all at once
@Query("SELECT sn FROM SharedNote sn JOIN FETCH sn.note n JOIN FETCH n.author JOIN FETCH sn.sharedToUser WHERE sn.sharedToUser = :user")
Page<SharedNote> findBySharedToUser(@Param("user") User user, Pageable pageable);
```

**Why `JOIN FETCH` is safe here:** All fetched associations (`author`, `note`, `sharedToUser`) are `@ManyToOne` — not collections. `JOIN FETCH` on collections with pagination causes Hibernate to load everything into memory first, which is a different problem. `@ManyToOne` + `JOIN FETCH` + pagination is always safe.

**Result:** List endpoints go from O(N) queries to O(1) regardless of page size.

### Connection Pooling (HikariCP)

Without pooling, every request opens and closes a new DB connection — expensive (TCP handshake, auth negotiation). HikariCP maintains a pool of pre-opened, reusable connections.

| Setting | Value | Why |
|---------|-------|-----|
| `maximum-pool-size` | 10 | Cap on concurrent DB connections |
| `minimum-idle` | 5 | Always 5 warm connections ready, no cold-start delay |
| `connection-timeout` | 10s | Fail fast instead of hanging 30s |
| `idle-timeout` | 5min | Reclaim idle connections above min-idle |
| `max-lifetime` | 10min | **Critical** — retires connections before MySQL's `wait_timeout` kills them, preventing stale connection errors on long-running deployments |

The `max-lifetime` setting is the most important. Without it, connections alive past MySQL's `wait_timeout` get silently closed on the DB side — HikariCP hands out a dead connection and the next request fails with a cryptic error.

### Lazy Loading

All `@ManyToOne` relations use `FetchType.LAZY`. Data is only fetched when explicitly needed. Combined with `JOIN FETCH` in queries, this gives precise control: nothing is loaded by accident, everything that's needed is loaded in one query.

---

## Transaction Management

`@Transactional` is placed on `doService()` in every service:

**Write services** → `@Transactional`
- Wraps the entire operation (read → validate → write) in a single DB transaction
- If anything fails mid-way, the whole thing rolls back — no partial state in the DB
- `@Modifying` queries in repositories join the outer service transaction (Spring's default `REQUIRED` propagation)

**Read services** → `@Transactional(readOnly = true)`
- Tells Hibernate to skip dirty checking (no need to track entity state for flushing)
- Can allow DB-level read optimizations

**Special case — RefreshTokenRequestService** → `@Transactional(noRollbackFor = InvalidRefreshTokenException.class)`
- The revoke-all-on-stolen-token security measure fires a write then throws an exception
- Without `noRollbackFor`, Spring would roll back the revoke — defeating the security purpose
- This annotation ensures the revoke commits even when the exception propagates

---

## Exception Handling

### Global Exception Handler

`@RestControllerAdvice` on `GlobalExceptionHandler` — one central place that catches all exceptions and maps them to the correct HTTP response. No try-catch blocks in controllers or services.

### Custom Exception Hierarchy

All custom exceptions extend `ApiException` which carries a `ResponseOutcome`:

```
ApiException
├── ResourceNotFoundException  (404) — note/user/sharedNote not found
├── UnauthorizedException      (403) — not owner, no write permission
├── DuplicateResourceException (409) — username exists, note already shared
├── InvalidCredentialsException (401) — wrong password
└── InvalidRefreshTokenException (401) — expired/invalid/revoked token
```

Each exception has named static factory methods:
```java
ResourceNotFoundException.noteNotFound(id)
UnauthorizedException.notOwner(username)
InvalidRefreshTokenException.revoked()
```

This makes throw sites readable and consistent — no raw strings or magic numbers anywhere.

### ResponseOutcome Enum

Every API response carries a `ResponseOutcome` value with `(success, code, description, httpStatus)`. Controllers read the HTTP status directly from the outcome — no hardcoded status codes in controller logic.

---

## Configuration & Environment

### Environment Profiles

Spring profile system separates dev and prod config cleanly:

| | `application-dev.properties` | `application-prod.properties` |
|-|------------------------------|-------------------------------|
| SQL logging | ON | OFF |
| `ddl-auto` | `update` (auto-alter schema) | `validate` (crash if mismatch) |
| App log level | DEBUG | INFO |
| Swagger UI | Enabled | **Disabled** |
| CORS origin | `http://localhost:3000` | `${CORS_ALLOWED_ORIGINS}` |

Active profile set by `SPRING_PROFILES_ACTIVE` env var — `dev` in `.env` locally, `prod` on the server.

**`ddl-auto=validate` in prod** is a deliberate safety measure: if deployed code's entities don't match the DB schema, the app **refuses to start** rather than silently misbehaving or auto-altering production data.

### Secrets Management

All secrets live in environment variables — never hardcoded, never committed to git:

| Variable | What it is |
|----------|-----------|
| `DB_URL` | MySQL JDBC connection string |
| `DB_USERNAME` / `DB_PASSWORD` | DB credentials |
| `JWT_SECRET` | Base64-encoded signing key for JWT |
| `SPRING_PROFILES_ACTIVE` | `dev` or `prod` |
| `CORS_ALLOWED_ORIGINS` | Frontend URL (prod only) |

Locally loaded from `.env` via `spring-dotenv`. On AWS, set as system environment variables on the EC2 instance.

---

## AWS Hosting

### Infrastructure

```
Internet
    │
    ▼
EC2 Instance (ap-southeast-2)
  └── Spring Boot JAR running on JVM
         │
         ▼ (private VPC subnet)
RDS MySQL Instance (ap-southeast-2)
  └── MySQL 8.0
```

### EC2 — Application Server

- Spring Boot app packaged as a JAR (`./mvnw clean package`)
- Runs on EC2 in the same AWS region as RDS
- Environment variables set on the instance — no `.env` file on server
- `SPRING_PROFILES_ACTIVE=prod` activates production config

### RDS — Database

- Managed MySQL 8 on AWS RDS
- In the same VPC as EC2 — traffic never leaves AWS's private network
- EC2 connects via JDBC URL: `jdbc:mysql://<rds-endpoint>:3306/note_app`
- RDS handles automated backups, patching, and failover

### Why RDS over self-managed MySQL

- Automated daily backups with point-in-time recovery
- Multi-AZ failover option
- No operational overhead managing MySQL installation, updates, disk

---

## Key Design Decisions — Summary for Interviews

| Decision | What | Why |
|----------|------|-----|
| Generic `Service<Req, Res>` | All services share a typed base class | Single responsibility, type safety, consistent pattern |
| JWT (15 min) + Refresh Token (7 days) | Short-lived access + long-lived refresh | JWT can't be revoked (stateless), so keep it short. Refresh token is in DB so it can be revoked |
| Token rotation + revoke-all on reuse | Each refresh token is single-use; stolen token triggers full revocation | Industry-standard defence against refresh token theft |
| `FetchType.LAZY` + `JOIN FETCH` | Lazy by default, explicit fetch in queries | Prevents accidental over-fetching; N+1 solved at query level |
| `@Transactional` at service level | Wraps read-validate-write in one DB transaction | Atomicity — no partial writes if something fails mid-operation |
| `GlobalExceptionHandler` | One class handles all exceptions | No try-catch noise in business logic; consistent error responses |
| `ddl-auto=validate` in prod | Schema validated on startup, never auto-modified | Prevents accidental data loss from entity/DB drift |
| HikariCP `max-lifetime=10min` | Connections retired before MySQL kills them | Prevents stale connection errors on long-running instances |
| Rate limiting on auth endpoints | Bucket4j token bucket, 10 req/min per IP | Mitigates brute-force login and credential stuffing attacks |
| Profiles (dev/prod) | Separate config files per environment | Swagger off in prod, strict schema validation, no SQL noise in logs |
