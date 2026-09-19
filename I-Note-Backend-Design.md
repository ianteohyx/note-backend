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
├── security/      - JWT filter, rate limiting filter, refresh token service, refresh cookie factory, auth service
├── services/
│   ├── request/   - Input POJOs per service
│   ├── reponse/   - Output POJOs per service (sic — the package is spelled this way in the code)
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

Deleting a note cascades to its shared records — both in JPA (`cascade = ALL` on `Note.sharedNotes`) and in the schema (`ON DELETE CASCADE` on the `sharednotes` foreign keys), so recipients never hold a dangling share.

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
userId     INT FK → users.id
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
   → JWT returned in the response body
   → refresh token returned as an HttpOnly Set-Cookie (never in the body)

2. Every protected request
   → client sends: Authorization: Bearer <JWT>
   → JwtAuthenticationFilter validates JWT
   → sets SecurityContext with authenticated user
   → request proceeds to controller

3. POST /api/users/refresh (when JWT expires, or on app start after a page reload)
   → no body — the browser sends the refreshToken cookie automatically
   → 401 if the cookie is missing
   → validate: not expired, not revoked, exists in DB
   → revoke old token, issue new refresh token (rotation)
   → issue new JWT
   → new JWT in the body, rotated refresh token in a new Set-Cookie

4. POST /api/users/logout
   → no body — reads the refreshToken cookie
   → revokes that refresh token in the DB (if it exists and isn't already revoked)
   → responds with an expired cookie (Max-Age=0) so the browser drops it
   → always 200 — idempotent, never fails on a missing/unknown/already-revoked token
```

### JWT

- Library: `jjwt` 0.12.6
- Expiry: **15 minutes** — short-lived intentionally, limits damage if stolen
- Signing key: `${JWT_SECRET}` — Base64-encoded, stored as env var (never in code)
- Stateless — server doesn't store JWTs, validated by signature only

### Refresh Token Delivery — HttpOnly Cookie

The refresh token is the long-lived credential (7 days), so it is delivered where JavaScript can't read it. `RefreshTokenCookieFactory` builds a `Set-Cookie` with:

| Attribute | Value | Why |
|-----------|-------|-----|
| `HttpOnly` | always | Script can't read it, so an XSS payload can't exfiltrate a 7-day credential |
| `Path` | `/api/users` | Only the refresh/logout endpoints ever receive the cookie — it isn't attached to note or share requests |
| `Max-Age` | 7 days | Matches the DB expiry (`jwt.refresh-expiration-ms`) |
| `Secure` | `false` in dev, `true` in prod | No HTTPS locally; required in prod |
| `SameSite` | `Lax` in dev, `None` in prod | Prod frontend is cross-site to the API, so the cookie must be sent cross-site; dev `localhost:5173` → `localhost:8080` is same-site, so `Lax` works |

`LoginResponse.refreshToken` is `@JsonIgnore` — the service hands the token to the controller, and the controller moves it into the cookie, so it can never be serialized into a response body by accident. Path, `Secure` and `SameSite` are all overridable via env vars (see [Configuration](#configuration--environment)); if the frontend and API ever share a site, `SameSite=Lax` is the stronger choice.

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

**Logout:** `POST /api/users/logout` revokes the presented refresh token and clears the cookie. Without this, the still-valid cookie would silently re-authenticate the user on the next page load, so "logout" would only be cosmetic.

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

- Configured per environment — `http://localhost:5173` (the Vite dev server) in dev, explicit `${CORS_ALLOWED_ORIGINS}` in prod
- Only the known frontend origin can make credentialed cross-origin requests — no wildcard
- Credentials: `true` — required so the browser will send the refresh-token cookie on cross-origin `/refresh` and `/logout` calls (and it is why a wildcard origin isn't allowed)

### CSRF

CSRF protection is disabled (`csrf.disable()`) and the app is stateless (`SessionCreationPolicy.STATELESS`). That's sound here because:

- Note and share endpoints authenticate with the `Authorization: Bearer` header, which a forged cross-site request cannot attach — there's no ambient credential for an attacker to ride
- The one ambient credential, the refresh cookie, is `HttpOnly`, scoped to `Path=/api/users`, and only honoured by `/refresh` and `/logout`
- A forged call to those two can't read the new JWT (CORS blocks the response) and can't touch notes; the worst it can do is revoke or rotate the victim's own refresh token

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
| POST | `/api/users/refresh` | Public (needs the `refreshToken` cookie; no body) |
| POST | `/api/users/logout` | Public (revokes the cookie's refresh token; idempotent, always 200) |
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
| DELETE | `/api/shares/unshare` | Note owner only (batch revoke — body `{ unshares: [{ noteId, sharedToUsername }] }`) |
| PATCH | `/api/shares/permissions` | Note owner only (batch update — body `{ updates: [{ noteId, sharedToUsername, permission }] }`) |

`GET /api/shares/note/{noteId}/users` returns each recipient with their permission (`sharedUsers: [{ username, permission }]`), so a client can render and edit permissions from a single call.

### Batch Endpoints

Unshare and permission-change take a **list** of items rather than one path-variable per user/note. A client editing several recipients at once makes one request instead of N, and the whole batch runs in a single transaction (see [Transaction Management](#transaction-management)) — if any item fails validation (unknown user, not the owner, not shared), nothing is applied.

### Pagination

List endpoints (`GET /api/notes`, `GET /api/shares/received`) are paginated using Spring Data's `Pageable` (`page` 0-indexed, `size` default 10). Response includes `page`, `size`, `totalElements`, `totalPages` — clients can navigate large datasets without loading everything.

Both lists are sorted by the note's `dateModified` descending (latest-edited first). `GET /api/shares/received` sorts on `note.dateModified`, not the share record's own id — sorting by share id would order by when the note was *shared*, not when it was last edited.

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

The one query that does `LEFT JOIN FETCH` a *collection* is `findByIdWithSharedUsers` (a note with its shares and their users, for the "shared with" list). It's safe because it loads a single note and isn't paginated — the in-memory-pagination problem only bites when a collection fetch is combined with `Pageable`.

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

**Batch services — `UnshareNoteService`, `UpdateShareNotePermissionService`** → `@Transactional`
- The service loops over every item in the request inside one transaction
- A failure on any item (unknown user, not the owner, note not shared) throws and rolls back the whole batch — all-or-nothing, never a half-applied edit

**Special case — RefreshTokenRequestService** → `@Transactional(noRollbackFor = InvalidRefreshTokenException.class)`
- The revoke-all-on-stolen-token security measure fires a write then throws an exception
- Without `noRollbackFor`, Spring would roll back the revoke — defeating the security purpose
- This annotation ensures the revoke commits even when the exception propagates
- `RefreshTokenService.validateRefreshToken` carries the same annotation. Both are needed: the inner method joins the outer transaction, and without its own `noRollbackFor` the inner proxy would mark the shared transaction rollback-only as the exception passes through it

---

## Exception Handling

### Global Exception Handler

`@RestControllerAdvice` on `GlobalExceptionHandler` — one central place that catches all exceptions and maps them to the correct HTTP response. No try-catch blocks in controllers or services.

### Custom Exception Hierarchy

All custom exceptions extend `ApiException` which carries a `ResponseOutcome`:

```
ApiException
├── ResourceNotFoundException  (404) — note/user/sharedNote not found
│                                (`noteNotSharedToUser` uses `NOTE_NOT_SHARED`, which maps to 403)
├── UnauthorizedException      (403) — not owner, no write permission
├── DuplicateResourceException (409) — username exists, note already shared
├── InvalidCredentialsException (401) — wrong password
└── InvalidRefreshTokenException (401) — missing/expired/invalid/revoked token
```

The handler also covers two cases that aren't `ApiException`s:

- **Bean validation failures** (`@Valid` on request bodies) → `400` with `VALIDATION_ERROR` and a `fieldErrors` map of field → message, so clients can put each error next to its input
- **Anything unexpected** → logged with the stack trace, returned as a generic `500` `PROCESS_FAIL` with no internal detail leaked

A request with a missing/invalid/expired JWT never reaches the handler — Spring Security's authentication entry point answers `401` with `TOKEN_INVALID` directly.

Each exception has named static factory methods:
```java
ResourceNotFoundException.noteNotFound(id)
UnauthorizedException.notOwner(username)
InvalidRefreshTokenException.revoked()
```

This makes throw sites readable and consistent — no raw strings or magic numbers anywhere.

### ResponseOutcome Enum

Every API response carries a `ResponseOutcome` value with `(success, code, description, httpStatus)`. Controllers read the HTTP status directly from the outcome — the only hardcoded status is `201 Created` for successful create endpoints (signup, create note, share note).

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
| CORS origin | `http://localhost:5173` | `${CORS_ALLOWED_ORIGINS}` |
| Refresh cookie `Secure` | `false` (no HTTPS locally) | `true` (`${REFRESH_COOKIE_SECURE}`) |
| Refresh cookie `SameSite` | `Lax` | `None` (`${REFRESH_COOKIE_SAME_SITE}`) |

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
| `REFRESH_TOKEN_EXPIRATION_MS` | Optional — refresh token / cookie lifetime, default 7 days |
| `REFRESH_COOKIE_PATH` | Optional — cookie path, default `/api/users` |
| `REFRESH_COOKIE_SECURE` | Optional, prod — default `true` |
| `REFRESH_COOKIE_SAME_SITE` | Optional, prod — default `None` |

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
| Refresh token in an HttpOnly cookie | Sent via `Set-Cookie` (path `/api/users`), never in the JSON body | JavaScript can't read the long-lived credential, so XSS can't steal it. `@JsonIgnore` on the response field prevents leaking it into a body |
| Token rotation + revoke-all on reuse | Each refresh token is single-use; stolen token triggers full revocation | Industry-standard defence against refresh token theft |
| Server-side logout | `/logout` revokes the refresh token and expires the cookie | Otherwise the surviving cookie would silently re-authenticate the user — logout would be cosmetic |
| CSRF disabled, stateless | Bearer header for data endpoints; cookie scoped to `/api/users` | No ambient credential on note/share endpoints, so nothing for a forged request to ride |
| Batch unshare / permission endpoints | List-bodied requests, one transaction each | One round-trip for many edits; all-or-nothing, never half-applied |
| `FetchType.LAZY` + `JOIN FETCH` | Lazy by default, explicit fetch in queries | Prevents accidental over-fetching; N+1 solved at query level |
| `@Transactional` at service level | Wraps read-validate-write in one DB transaction | Atomicity — no partial writes if something fails mid-operation |
| `GlobalExceptionHandler` | One class handles all exceptions | No try-catch noise in business logic; consistent error responses |
| `ddl-auto=validate` in prod | Schema validated on startup, never auto-modified | Prevents accidental data loss from entity/DB drift |
| HikariCP `max-lifetime=10min` | Connections retired before MySQL kills them | Prevents stale connection errors on long-running instances |
| Rate limiting on auth endpoints | Bucket4j token bucket, 10 req/min per IP | Mitigates brute-force login and credential stuffing attacks |
| Profiles (dev/prod) | Separate config files per environment | Swagger off in prod, strict schema validation, no SQL noise in logs |
