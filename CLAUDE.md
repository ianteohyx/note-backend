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
├── security/        - JWT filter, rate limiting filter, auth service, refresh token service
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
| POST | `/api/users/login` | LogInService | Login, returns JWT + refresh token |
| POST | `/api/users/refresh` | RefreshTokenRequestService | Rotate refresh token, get new JWT |

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
1. Extend `Service<YourRequest, YourResponse>`
2. Implement `doService()` with business logic
3. Annotate with `@org.springframework.stereotype.Service`

Throw custom exceptions for errors (handled globally by `GlobalExceptionHandler`).

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
| `LoginResponse` | `+ token (JWT), refreshToken` |
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
- **Token rotation**: on each refresh, old token is revoked and a new one is issued
- **Security**: if a revoked token is used, ALL tokens for that user are immediately revoked
- Cleanup: scheduled daily at 3 AM via `ScheduledTasks`

### Rate Limiting
- Library: Bucket4j 8.10.1
- Applies to: `/api/users/signup`, `/api/users/login`, `/api/users/refresh`
- Limit: 10 requests/minute per IP
- IP detection: `X-Forwarded-For` header or remote address
- Response on exceeded: HTTP 429 + `RATE_LIMIT_EXCEEDED` outcome

### CORS (SecurityConfig)
- Allowed origins: `${cors.allowed-origins:http://localhost:3000}`
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
| `REFRESH_TOKEN_EXPIRATION_MS` | No | 604800000 (7d) | Refresh token TTL |
| `cors.allowed-origins` | No | http://localhost:3000 | CORS allowed origin |
| `jwt.expiration-ms` | No | 900000 (15m) | JWT access token TTL |

---

## Database

MySQL via AWS RDS. Physical naming strategy is set to `PhysicalNamingStrategyStandardImpl` so entity/column names map literally (no snake_case conversion).
