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
- `DB_USERNAME` / `DB_PASSWORD` are the credentials the container creates and the app logs in with. Pick any values.
- `DB_ROOT_PASSWORD` is the MySQL root password. Pick any value.

MySQL only reads these on the **first** start with an empty volume. If you change them later, run `docker compose down -v` to recreate the container with the new values (this wipes the data).

### 2. Start the database

```bash
docker compose up -d
```

This starts a MySQL 8 container and creates the empty tables from `docker/schema.sql` (no data). Sign up through the API to create your first user. Data is persisted in a Docker volume, so it survives container restarts.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Other Commands

```bash
# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Stop the database container
docker compose down

# Stop and delete all data (wipes the volume)
docker compose down -v
```
