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

### 2. Start the database

```bash
docker compose up -d
```

This starts a MySQL 8 container with the schema and seed data already loaded. Data is persisted in a Docker volume, so it survives container restarts.

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
