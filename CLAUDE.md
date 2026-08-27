# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Self-reminder:** After completing any task that introduces new endpoints, models, entities, exceptions, response outcomes, services, environment variables, or changes to auth/security flow — update this file to reflect those changes before ending the conversation.

## Build & Run Commands

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=TestClassName

# Compile only
./mvnw compile
```

## Architecture

This is a Spring Boot 3.5.5 REST API for a collaborative note-taking application using Java 17.

### Package Structure

```
src/main/java/com/yx/note_app/
├── config/          - SecurityConfig, ScheduledTasks
├── controllers/     - UserController, NoteController, ShareNoteController
├── dto/             - DTOs (NoteDto, SharedNoteDto, base Dto)
├── enums/           - Permission, ResponseOutcome
├── exception/       - Custom exceptions + GlobalExceptionHandler
├── models/          - JPA entities (User, Note, SharedNote, RefreshToken)
├── repositories/    - Spring Data JPA interfaces
├── security/        - JWT filter, rate limiting filter, auth service, refresh token service, RefreshTokenCookieFactory
├── services/
│   ├── request/     - Request POJOs for each service
│   ├── response/    - Response POJOs (ApiResponse subclasses)
│   └── service/     - Business logic (all extend abstract Service<Req, Res>)
└── utils/
    ├── jwt/         - JwtUtils
    └── mapper/      - Model2Dto mappers (Note2NoteDto, SharedNote2SharedNoteDto)
```

---

## API Endpoints

### Public (no auth required)
| Method | Path | Service | Description |
|--------|------|---------|-------------|
| POST | `/api/users/signup` | SignUpService | Register new user |
| POST | `/api/users/login` | LogInService | Login, returns JWT in body + refresh token as HttpOnly cookie |
| POST | `/api/users/refresh` | RefreshTokenRequestService | Reads `refreshToken` cookie, rotates it (new cookie), returns new JWT. No request body. |

### Protected (requires `Authorization: Bearer <token>`)
| Method | Path | Service | Description |
|--------|------|---------|-------------|
| POST | `/api/notes` | AddNoteService | Create note (201) |
| GET | `/api/notes` | GetAllNotesService | Get all user's notes |
| GET | `/api/notes/{id}` | GetSingleNoteService | Get single note (owner only) |
| PATCH | `/api/notes/{id}` | UpdateNoteService | Update note title/content |
| DELETE | `/api/notes/{id}` | DeleteNoteService | Delete note (cascades shared records) |
| POST | `/api/shares` | ShareNoteToOthersService | Share note with user (201) |
| GET | `/api/shares/received` | GetAllSharedToMeService | Get all notes shared to me |
| GET | `/api/shares/{id}` | GetSingleSharedNoteService | Get single shared note (recipient only) |
| PATCH | `/api/shares/{id}` | EditSharedNoteService | Edit shared note (WRITE permission only) |
| GET | `/api/shares/note/{noteId}/users` | GetSharedToUsersService | List users note is shared to (owner only) |
| DELETE | `/api/shares/note/{noteId}/user/{username}` | UnshareNoteService | Revoke access (owner only) |
| PATCH | `/api/shares/note/{noteId}/user/{username}/permission` | UpdateShareNotePermissionService | Change READ/WRITE permission (owner only) |

---

## Service Pattern

All services extend `Service<Request, Response>`:
- `execute(Request)` - entry point called by controllers
- `doService(Request)` - implement business logic here (abstract)
- `getUserUsingTheService()` - returns current authenticated `User` from security context

To create a new service:
1. Extend `Service<YourRequest, YourResponse>` — use the specific response class, not `ApiResponse`
2. Implement `doService()` with the specific return type matching the generic parameter
3. Annotate with `@org.springframework.stereotype.Service`
4. Add `@Transactional` on `doService()` for write operations, `@Transactional(readOnly = true)` for read-only operations

Throw custom exceptions for errors (handled globally by `GlobalExceptionHandler`).

### Authorization Helpers (base `Service` class)

Do NOT manually check ownership with `.equals()`. Use the centralized helpers inherited from `Service`:

| Method | Use when |
|--------|----------|
| `assertIsOwner(Note note)` | Current user must be the note's author |
| `assertIsRecipient(SharedNote sharedNote)` | Current user must be the shared-to user |
| `assertHasWritePermission(SharedNote sharedNote)` | Current user must be recipient AND have WRITE permission |

These throw `UnauthorizedException` automatically if the check fails.

---

## Models / Entities

### User
```
Table: users
- id: int (PK, auto-increment)
- username: String (unique, not null)
- password: String (BCrypt encoded)
- notes: List<Note> (OneToMany, cascade ALL)
- sharedNotes: List<SharedNote> (OneToMany, cascade ALL)
```

### Note
```
Table: notes
- id: int (PK)
- title: String (not null)
- content: String (TEXT)
- author: User (ManyToOne)
- sharedNotes: List<SharedNote> (OneToMany, cascade ALL)
- dateCreated: LocalDateTime (@PrePersist)
- dateModified: LocalDateTime (@PrePersist / @PreUpdate)
```

### SharedNote
```
Table: sharednotes
- id: int (PK)
- note: Note (ManyToOne)
- sharedToUser: User (ManyToOne)
- permission: Permission enum (READ | WRITE)
- Unique constraint: (note_id, shared_to_user_id)
```

### RefreshToken
```
Table: refreshtokens
- token: String (PK, UUID)
- user: User (ManyToOne, lazy)
- expiryDate: Instant
- revoked: boolean
- createdAt: Instant (@PrePersist)
- isExpired() / isRevoked() - helper methods
```

---

## DTOs & Responses

All responses extend `ApiResponse` which carries a `ResponseOutcome`.

| Class | Fields |
|-------|--------|
| `ApiResponse` | `responseOutcome` |
| `ErrorResponse` | `+ message, fieldErrors: Map<String,String>` |
| `LoginResponse` | `+ token (JWT)` — refresh token is NOT in the body; it is sent as the `refreshToken` HttpOnly cookie (`refreshToken` field is `@JsonIgnore`, used only internally to pass the value from service to controller) |
| `GetAllNoteResponse` | `+ notes: List<NoteDto>` |
| `GetSingleNoteResponse` | `+ noteDto` |
| `GetAllSharedToMeResponse` | `+ sharedNotes: List<SharedNoteDto>` |
| `GetSingleSharedNoteResponse` | `+ sharedNote` |
| `GetSharedToUsersResponse` | `+ usernames: List<String>` |

**NoteDto**: `id, title, content, authorName, dateCreated, dateModified`
**SharedNoteDto**: `id, note: NoteDto, permission`

Use `ResponseDirectory.buildSuccessResponse()` / `buildFailResponse(ResponseOutcome)` for constructing responses.

---

## Enums

### ResponseOutcome
Each value carries `(success: boolean, code, desc, httpStatus)`.

| Value | HTTP Status | Meaning |
|-------|-------------|---------|
| SUCCESS | 200 | OK |
| PROCESS_FAIL | 500 | Internal error |
| PARAM_ILLEGAL | 400 | Bad request params |
| VALIDATION_ERROR | 400 | Bean validation failed |
| USERNAME_EXIST | 409 | Duplicate username |
| USER_NOT_EXIST | 404 | User not found |
| PASSWORD_INVALID | 400 | Password format invalid |
| LOGIN_FAIL | 401 | Wrong credentials |
| TOKEN_INVALID | 401 | JWT invalid |
| REFRESH_TOKEN_INVALID | 401 | Refresh token invalid/expired/revoked |
| RATE_LIMIT_EXCEEDED | 429 | Too many requests |
| NOTE_NOT_EXIST | 404 | Note not found |
| NOTE_NOT_SHARED | 403 | Note not shared to user |
| NOTE_ALREADY_SHARED | 409 | Duplicate share |
| ACTION_NOT_ALLOWED | 403 | Not owner / no permission |

### Permission
`READ` - read-only access | `WRITE` - read + write access

---

## Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps all exceptions to `ErrorResponse` + correct HTTP status automatically.

Custom exceptions (all extend `ApiException`):

| Exception | Static Factories | Default Outcome |
|-----------|-----------------|-----------------|
| `ResourceNotFoundException` | `noteNotFound(id)`, `userNotFound(username)`, `sharedNoteNotFound(id)`, `noteNotSharedToUser(noteId, username)` | NOTE_NOT_EXIST (404) |
| `UnauthorizedException` | `notOwner(username)`, `noEditPermission(username)` | ACTION_NOT_ALLOWED (403) |
| `DuplicateResourceException` | `usernameExists(username)`, `noteAlreadyShared(noteId, username)` | varies |
| `InvalidCredentialsException` | `loginFailed()` | LOGIN_FAIL (401) |
| `InvalidRefreshTokenException` | `expired()`, `invalid()`, `revoked()` | REFRESH_TOKEN_INVALID (401) |

Always throw these instead of returning error codes manually from services.

---

## Authentication & Security

### Filter Chain Order
1. `RateLimitingFilter` - rate limiting on auth endpoints
2. `JwtAuthenticationFilter` - validates JWT, sets SecurityContext
3. Spring Security filters

### JWT
- Library: `jjwt` 0.12.6
- Expiration: 15 min (configurable via `jwt.expiration-ms`, default 900000)
- Secret: `${JWT_SECRET}` (env var, Base64-encoded)
- Passed as: `Authorization: Bearer <token>`

### Refresh Token
- Storage: DB (`refreshtokens` table)
- Expiration: 7 days (configurable via `REFRESH_TOKEN_EXPIRATION_MS`, default 604800000)
- **Delivery**: sent to the client only as an **HttpOnly cookie** named `refreshToken` (never in the response body). Built by `RefreshTokenCookieFactory` (in `security/`).
  - `Set-Cookie` on `POST /api/users/login` and `POST /api/users/refresh` responses
  - `POST /api/users/refresh` takes **no body** — it reads the `refreshToken` cookie via `@CookieValue`; missing/blank cookie → `InvalidRefreshTokenException.invalid()` (401)
  - Cookie attributes: `HttpOnly`, `Secure` (configurable), `SameSite` (configurable), `Path` (configurable, default `/api/users`), `Max-Age` = refresh token TTL
  - **dev profile**: `Secure=false`, `SameSite=Lax` — works over `http://localhost` and for a same-site frontend (`localhost:5173` → `localhost:8080`), and Postman sends it automatically via its cookie jar
  - **prod profile**: `Secure=true`, `SameSite=None` — required when the deployed frontend is on a different site; also needs CORS `allowCredentials=true` (already set) and the frontend using `credentials: 'include'` / `withCredentials: true`
- **Token rotation**: on each refresh, old token is revoked and a new one is issued (new cookie)
- **Security**: if a revoked token is used, ALL tokens for that user are immediately revoked
- Cleanup: scheduled daily at 3 AM via `ScheduledTasks`

### Rate Limiting
- Library: Bucket4j 8.10.1
- Applies to: `/api/users/signup`, `/api/users/login`, `/api/users/refresh`
- Limit: 10 requests/minute per IP
- IP detection: `X-Forwarded-For` header or remote address
- Response on exceeded: HTTP 429 + `RATE_LIMIT_EXCEEDED` outcome

### CORS (SecurityConfig)
- Allowed origins: `${cors.allowed-origins}` (set via env var — `http://localhost:3000` in dev, explicit URL in prod)
- Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Credentials: true

---

## Repositories

| Repository | Notable Custom Methods |
|------------|----------------------|
| `UserRepository` | `findByUsername`, `existsByUsername` |
| `NoteRepository` | `findByAuthor`, `@Modifying updateNote(id, title, content)` |
| `ShareNoteRepository` | `findBySharedToUser`, `findByNoteIdAndSharedToUserId`, `existsByNoteIdAndSharedToUserId`, `@Modifying updateSharedNotePermission(id, permission)` |
| `RefreshTokenRepository` | `findByToken`, `@Modifying revokeAllByUser(user)`, `@Modifying deleteExpiredTokens(now)` |

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_URL` | Yes | - | MySQL JDBC URL |
| `DB_USERNAME` | Yes | - | DB username |
| `DB_PASSWORD` | Yes | - | DB password |
| `JWT_SECRET` | Yes | - | Base64-encoded JWT signing key |
| `SPRING_PROFILES_ACTIVE` | Yes | - | Active profile: `dev` or `prod` |
| `REFRESH_TOKEN_EXPIRATION_MS` | No | 604800000 (7d) | Refresh token TTL |
| `cors.allowed-origins` | No (dev) / Yes (prod) | - | CORS allowed origin |
| `CORS_ALLOWED_ORIGINS` | Yes (prod) | - | Frontend URL for CORS in prod profile |
| `jwt.expiration-ms` | No | 900000 (15m) | JWT access token TTL |
| `REFRESH_COOKIE_PATH` | No | `/api/users` | `Path` attribute of the `refreshToken` cookie |
| `REFRESH_COOKIE_SECURE` | No (prod only) | `true` | `Secure` attribute of the `refreshToken` cookie. dev profile hard-codes `false`; prod defaults `true` |
| `REFRESH_COOKIE_SAME_SITE` | No (prod only) | `None` | `SameSite` attribute of the `refreshToken` cookie. dev profile hard-codes `Lax`; prod defaults `None` (cross-site frontend) |

Set in `.env` for local dev. Set as server env vars on AWS EC2 for prod.

---

## Environment Profiles

| Profile | File | When used |
|---------|------|-----------|
| `dev` | `application-dev.properties` | Local development (SQL logging on, `ddl-auto=update`, DEBUG logging) |
| `prod` | `application-prod.properties` | AWS deployment (no SQL logging, `ddl-auto=validate`, Swagger disabled) |

Active profile is controlled by `SPRING_PROFILES_ACTIVE` env var.

- **`ddl-auto=update` (dev)**: auto-adds missing columns/tables but does NOT rename or drop — mismatched column names still require manual DB fix
- **`ddl-auto=validate` (prod)**: startup fails if schema doesn't match entities — protects against silent mismatches

---

## Database

MySQL via AWS RDS. Physical naming strategy is set to `PhysicalNamingStrategyStandardImpl` so entity/column names map literally (no snake_case conversion).

---

## Connection Pool (HikariCP)

Configured in `application.properties`:

| Setting | Value | Purpose |
|---------|-------|---------|
| `maximum-pool-size` | 10 | Max concurrent DB connections |
| `minimum-idle` | 5 | Warm connections kept at all times |
| `connection-timeout` | 10,000ms | Fail fast if pool exhausted |
| `idle-timeout` | 300,000ms | Close idle connections above min-idle after 5min |
| `max-lifetime` | 600,000ms | Retire connections after 10min — prevents stale connection errors from MySQL `wait_timeout` |

---

## N+1 Query Prevention

All paginated list queries use `JOIN FETCH` to avoid N+1:
- `NoteRepository.findByAuthor(user, pageable)` — fetches `author` in same query
- `ShareNoteRepository.findBySharedToUser(user, pageable)` — fetches `note`, `note.author`, and `sharedToUser` in same query

All `@ManyToOne` relations are `FetchType.LAZY` by default. Always use `JOIN FETCH` in queries that iterate over results.
