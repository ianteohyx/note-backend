# I-Note Backend

REST API for the I-Note collaborative note-taking app, built with Spring Boot 3.5.5 and Java 17.

## Prerequisites

- Java 17+
- Docker

## Local Setup

### 1. Configure environment variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

The database values are ones you choose yourself. Docker uses them to create the MySQL user and database on first start, and the app uses the same values to connect, so they must match:

- `DB_NAME` must be the database name at the end of `DB_URL` (e.g. `jdbc:mysql://localhost:3306/<DB_NAME>`).
- `DB_USERNAME` / `DB_PASSWORD` — pick any values, **but not `root`** for `DB_USERNAME` (the container will fail to start).
- `DB_ROOT_PASSWORD` — the MySQL root password. Pick a different value than `DB_PASSWORD`.

MySQL only reads these on the **first** start with an empty volume. If you change them later, run `docker compose down -v` to recreate the container with the new values (this wipes the data).

### 2. Start the backend

**Option A — everything in Docker:**

```bash
docker compose up -d --build
```

Starts `db` and `app` together, builds the backend image, initializes the schema on first run. API at `http://localhost:8080`.

**Option B — backend via your IDE/Maven, only the database in Docker:**

```bash
docker compose up -d db
./mvnw spring-boot:run
```

Use this for active backend development, so your IDE's run/debug/hot-reload applies.

Either way: sign up through the API to create your first user. Data persists in a Docker volume across restarts — only `docker compose down -v` wipes it.

Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Frontend

Separate repo: [note-frontend](https://github.com/ianteohyx/note-frontend)

## Other Commands

```bash
# Run tests
./mvnw test

# Build JAR (native, no Docker)
./mvnw clean package

# Rebuild the backend image after a code change, then restart
docker compose up -d --build

# Follow the backend container's logs
docker compose logs -f app

# Stop containers, keep data (fast restart with `docker compose start`)
docker compose stop

# Stop and remove containers + network, keep data
docker compose down

# Stop and wipe all data too — next start is a fresh DB
docker compose down -v
```

## Production

See `docker-compose.prod.yml` for the production setup (backend only — frontend is on S3 + CloudFront; pulls a pre-built image, no `db` service — uses AWS RDS instead).
