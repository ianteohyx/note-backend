# syntax=docker/dockerfile:1

# ========================
# Stage 1: Build
# ========================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy wrapper + pom first so dependency resolution is cached
# in its own layer and only re-runs when pom.xml actually changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Now copy source and build the jar
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ========================
# Stage 2: Run
# ========================
FROM eclipse-temurin:17-jre-jammy AS run
WORKDIR /app

# Run as a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JAVA_OPTS lets you pass heap sizing etc. at `docker run` time,
# e.g. -e JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Basic TCP-level liveness probe (no actuator dependency yet — see README/CLAUDE.md
# note below if you add spring-boot-starter-actuator later, switch this to
# `curl -f http://localhost:8080/actuator/health`).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/localhost/8080' || exit 1
