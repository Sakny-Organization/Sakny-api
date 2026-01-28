# Sakny API: Modular Monolith Architecture

This document explains the architectural design of the Sakny API, how it is structured, and the workflow for development and deployment.

## 1. What is a Modular Monolith?

A **Modular Monolith** is a software design pattern where the code is divided into independent, logical modules, but is deployed as a **single unit** (one JAR, one container).

- **Modularity**: Code is organized by feature (e.g., Auth, Matching, User).
- **Deployment**: No complex microservice orchestration needed. One server, one port.

---

## 2. Project Structure

The project follows a standard Maven Multi-Module hierarchy:

```text
sakny-api/
├── .env                        # Single source of truth for environment variables
├── .env.example                # Template for environment variables
├── .gitignore                  # Git ignore rules
├── ARCHITECTURE.md             # This documentation
├── Dockerfile                  # Multi-stage production Docker build
├── docker-compose.yml          # Orchestration (Postgres, Redis, Sakny Server)
├── mvnw                        # Maven wrapper script (Unix)
├── mvnw.cmd                    # Maven wrapper script (Windows)
├── pom.xml                     # Root Parent POM (Manages versions and modules)
│
├── common/                     # SHARED LIBRARY MODULE
│   ├── src/main/java/          # Shared DTOs, Exceptions, and Utils
│   └── pom.xml                 # Common module configuration
│
├── auth/                       # FEATURE MODULE (Library Mode)
│   ├── src/main/java/          # Auth controllers, services, repositories
│   └── pom.xml                 # Auth module configuration
│
└── sakny-server/               # THE RUNNER (Main Entry Point)
    ├── src/main/java/          # Contains SaknyApplication.java (ONLY main class)
    ├── src/main/resources/
    │   ├── db/changelog/       # Centralized Liquibase migrations
    │   │   ├── changes/        # Individual SQL/XML migration files
    │   │   └── db.changelog-master.xml
    │   └── application.yml     # Global application configuration
    └── pom.xml                 # Main server configuration (packages the final JAR)
```

### Module Roles:
1. **`common`**: A library shared across all modules. It doesn't run on its own.
2. **`auth`**: Contains all controllers, services, and repositories related to security. It is packaged as a JAR and loaded by the server.
3. **`sakny-server`**: This is the only module with a `main` class (`@SpringBootApplication`). It aggregates all other modules.

---

## 3. Configuration & Database

### Single Source of Truth
We use a unified configuration strategy:
- **`application.yml`**: Located in `sakny-server`. It contains global settings for the entire platform.
- **`sakny_db`**: A single PostgreSQL database shared by all modules.
- **Liquibase**: Centralized in `sakny-server`, managing all table migrations for all modules in one sequence.

---

## 4. Development Flow

### Adding a New Feature (e.g., "Matching")
To add a new module:
1. Create a new directory `matching/`.
2. Add a `pom.xml` to `matching/` with `sakny-platform` as the parent.
3. Add the new module to the root `pom.xml` (`<module>matching</module>`).
4. Add the `matching` dependency to `sakny-server/pom.xml`.
5. Spring Boot will automatically scan and load the new controllers/services.

---

## 5. Deployment Flow

Our deployment uses a **Docker Multi-Stage Build** defined in the root `Dockerfile`.

### Stage 1: Build
- Uses `maven:3.9.6-eclipse-temurin-17`.
- Copies `pom.xml` files first to **cache dependencies**.
- Compiles all modules and generates the final fat-JAR in `sakny-server/target/`.

### Stage 2: Run
- Uses `eclipse-temurin:17-jre-alpine` (lightweight, secure).
- Copies only the generated JAR from the build stage.
- Runs the application on port `8081`.

### Orchestration
The `docker-compose.yml` orchestrates the entire stack:
1. **Postgres**: Health-checked and initialized with `sakny_db`.
2. **Redis**: Used for caching and session management.
3. **Sakny Server**: Depends on Postgres and Redis being ready.

---

## 6. Commands

| Goal | Command |
| :--- | :--- |
| **Build Project** | `./mvnw clean install` |
| **Run Locally (Maven)** | `./mvnw spring-boot:run -pl sakny-server` |
| **Run (Docker)** | `docker-compose up -d --build` |
| **View Logs** | `docker logs -f sakny-server` |
